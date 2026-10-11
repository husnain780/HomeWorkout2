-- supabase/tests/database_security_test.sql
-- HomeWorkout2: Comprehensive Database Security and RLS Policy Verification Test Suite
-- Run this in the Supabase Dashboard SQL Editor or via automated test runners.
-- All operations are executed within a transaction that rolls back automatically.

begin;

-- 1. Setup mock environment, test UUIDs, and results reporter
create temporary table _test_vars (
    key text primary key,
    val text
);

create temporary table _test_results (
    test_num int primary key,
    test_name text not null,
    status text not null,
    detail text not null
);

grant all on table _test_results to authenticated;

insert into _test_vars values 
    ('user_a_id', 'a0000000-0000-0000-0000-000000000001'),
    ('user_b_id', 'b0000000-0000-0000-0000-000000000002'),
    ('admin_id',  'c0000000-0000-0000-0000-000000000003');

-- Temporarily bypass RLS as superuser to seed test fixtures in auth.users & public.profiles
set local role postgres;

insert into auth.users (id, email, raw_user_meta_data)
values 
    ('a0000000-0000-0000-0000-000000000001', 'usera@example.com', '{"full_name": "User A"}'::jsonb),
    ('b0000000-0000-0000-0000-000000000002', 'userb@example.com', '{"full_name": "User B"}'::jsonb),
    ('c0000000-0000-0000-0000-000000000003', 'admin@example.com', '{"full_name": "Admin User"}'::jsonb)
on conflict (id) do nothing;

-- Verify signup trigger assigned standard 'user' role
do $$
declare
    v_role text;
begin
    select role into v_role from public.profiles where id = 'a0000000-0000-0000-0000-000000000001';
    assert v_role = 'user', 'FAIL: handle_new_user trigger did not assign user role to User A';
    raise notice 'PASS: Signup trigger correctly assigned user role.';
    insert into _test_results values (1, 'Signup Trigger Role Assignment', 'PASS', 'handle_new_user correctly assigned user role to User A');
end $$;

-- Promote Admin test user directly via administrative function
update public.profiles set role = 'admin' where id = 'c0000000-0000-0000-0000-000000000003';

-- Seed workout histories
insert into public.workout_history (id, user_id, workout_id, workout_name, duration_minutes, exercises_completed, calories_burned)
values 
    ('w1_a', 'a0000000-0000-0000-0000-000000000001', 'full_body', 'Full Body A', 20, 5, 120),
    ('w2_b', 'b0000000-0000-0000-0000-000000000002', 'chest_hiit', 'Chest HIIT B', 30, 8, 200)
on conflict do nothing;

-- 2. TEST SUITE AS REGULAR AUTHENTICATED USER A
set local role authenticated;
set local "request.jwt.claim.sub" = 'a0000000-0000-0000-0000-000000000001';

-- Test 2.1: User A reading own profile (MUST SUCCEED)
do $$
declare
    v_count integer;
begin
    select count(*) into v_count from public.profiles where id = 'a0000000-0000-0000-0000-000000000001';
    assert v_count = 1, 'FAIL: User A cannot read own profile';
    raise notice 'PASS: User A can read own profile.';
    insert into _test_results values (2, 'User A Read Own Profile', 'PASS', 'User A read own profile record successfully (1 row)');
end $$;

-- Test 2.2: User A attempting to read User B's profile (MUST RETURN 0 ROWS)
do $$
declare
    v_count integer;
begin
    select count(*) into v_count from public.profiles where id = 'b0000000-0000-0000-0000-000000000002';
    assert v_count = 0, 'FAIL: User A was able to read User B profile';
    raise notice 'PASS: User A cannot read User B profile.';
    insert into _test_results values (3, 'User A Profile Isolation', 'PASS', 'User A received 0 rows attempting to read User B profile');
end $$;

-- Test 2.3: User A attempting to read User B's workout history (MUST RETURN 0 ROWS)
do $$
declare
    v_count integer;
begin
    select count(*) into v_count from public.workout_history where user_id = 'b0000000-0000-0000-0000-000000000002';
    assert v_count = 0, 'FAIL: User A was able to read User B workout history';
    raise notice 'PASS: User A cannot read User B workout history.';
    insert into _test_results values (4, 'User A Workout History Read Isolation', 'PASS', 'User A received 0 rows attempting to read User B workout history');
end $$;

-- Test 2.4: User A attempting to update or delete User B's workout history (MUST AFFECT 0 ROWS)
do $$
declare
    v_rows integer;
begin
    update public.workout_history set workout_name = 'Hacked' where user_id = 'b0000000-0000-0000-0000-000000000002';
    get diagnostics v_rows = row_count;
    assert v_rows = 0, 'FAIL: User A updated User B workout history';

    delete from public.workout_history where user_id = 'b0000000-0000-0000-0000-000000000002';
    get diagnostics v_rows = row_count;
    assert v_rows = 0, 'FAIL: User A deleted User B workout history';

    raise notice 'PASS: User A cannot alter or delete User B workout history.';
    insert into _test_results values (5, 'User A Workout History Write Isolation', 'PASS', '0 rows affected on update and delete attempts');
end $$;

-- Test 2.5: User A attempting privilege escalation to admin role (MUST FAIL)
do $$
declare
    v_escaped boolean := false;
    v_err_msg text := '';
begin
    begin
        update public.profiles set role = 'admin' where id = 'a0000000-0000-0000-0000-000000000001';
        v_escaped := true;
    exception when others then
        v_err_msg := sqlerrm;
        raise notice 'PASS: Privilege escalation blocked as expected: %', v_err_msg;
    end;
    assert not v_escaped, 'FAIL: User A was able to escalate role to admin!';
    insert into _test_results values (6, 'Privilege Escalation Prevention', 'PASS', 'Self-promotion blocked: ' || v_err_msg);
end $$;

-- Test 2.6: User A calling get_admin_stats (MUST FAIL)
do $$
declare
    v_escaped boolean := false;
    v_err_msg text := '';
begin
    begin
        perform public.get_admin_stats();
        v_escaped := true;
    exception when others then
        v_err_msg := sqlerrm;
        raise notice 'PASS: Unauthorized call to get_admin_stats rejected as expected: %', v_err_msg;
    end;
    assert not v_escaped, 'FAIL: User A was able to call get_admin_stats!';
    insert into _test_results values (7, 'Unauthorized Admin RPC Prevention', 'PASS', 'get_admin_stats rejected: ' || v_err_msg);
end $$;

-- 3. TEST SUITE AS ADMIN USER C
set local role authenticated;
set local "request.jwt.claim.sub" = 'c0000000-0000-0000-0000-000000000003';

-- Test 3.1: Admin C reading all profiles (MUST SEE ALL)
do $$
declare
    v_count integer;
begin
    select count(*) into v_count from public.profiles;
    assert v_count >= 3, 'FAIL: Admin cannot see all profiles';
    raise notice 'PASS: Admin C can view all profiles.';
    insert into _test_results values (8, 'Admin Profile Cross-Tenant Visibility', 'PASS', 'Admin observed ' || v_count || ' profile records');
end $$;

-- Test 3.2: Admin C reading all workout histories (MUST SEE ALL)
do $$
declare
    v_count integer;
begin
    select count(*) into v_count from public.workout_history;
    assert v_count >= 2, 'FAIL: Admin cannot view all workout history';
    raise notice 'PASS: Admin C can view all workout histories.';
    insert into _test_results values (9, 'Admin Workout History Cross-Tenant Visibility', 'PASS', 'Admin observed ' || v_count || ' workout records');
end $$;

-- Test 3.3: Admin C calling get_admin_stats (MUST SUCCEED)
do $$
declare
    v_stats json;
begin
    v_stats := public.get_admin_stats();
    assert v_stats->>'total_users' is not null, 'FAIL: Admin stats missing total_users';
    raise notice 'PASS: Admin C successfully executed get_admin_stats(): %', v_stats;
    insert into _test_results values (10, 'Admin Analytics RPC Execution', 'PASS', 'Executed successfully: ' || v_stats::text);
end $$;

-- Seed saved workout plans and ai coach messages for User B as superuser
set local role postgres;
insert into public.saved_workout_plans (id, user_id, title, description, goal, difficulty, days_per_week, minutes_per_workout)
values ('plan_b_1', 'b0000000-0000-0000-0000-000000000002', 'User B Secret Plan', 'Private AI Plan', 'Strength', 'Advanced', 4, 30)
on conflict do nothing;

insert into public.ai_coach_messages (id, user_id, message_text, is_user)
values ('msg_b_1', 'b0000000-0000-0000-0000-000000000002', 'My private AI coach prompt', true)
on conflict do nothing;

-- Switch back to User A to verify isolation on new tables
set local role authenticated;
set local "request.jwt.claim.sub" = 'a0000000-0000-0000-0000-000000000001';

-- Test 4.1: Saved Workout Plans RLS Isolation
do $$
declare
    v_count integer;
begin
    select count(*) into v_count from public.saved_workout_plans where user_id = 'b0000000-0000-0000-0000-000000000002';
    assert v_count = 0, 'FAIL: User A was able to read User B saved workout plans';
    insert into _test_results values (11, 'Saved Workout Plans RLS Isolation', 'PASS', 'User A received 0 rows attempting to read User B saved plans');
end $$;

-- Test 4.2: AI Coach Messages RLS Isolation
do $$
declare
    v_count integer;
begin
    select count(*) into v_count from public.ai_coach_messages where user_id = 'b0000000-0000-0000-0000-000000000002';
    assert v_count = 0, 'FAIL: User A was able to read User B AI coach messages';
    insert into _test_results values (12, 'AI Coach Messages RLS Isolation', 'PASS', 'User A received 0 rows attempting to read User B coach messages');
end $$;

-- Test 4.3: User Notifications Trigger & RLS Isolation
do $$
declare
    v_own_count integer;
    v_other_count integer;
begin
    select count(*) into v_own_count from public.user_notifications where user_id = 'a0000000-0000-0000-0000-000000000001';
    select count(*) into v_other_count from public.user_notifications where user_id = 'b0000000-0000-0000-0000-000000000002';
    assert v_own_count >= 1, 'FAIL: Signup trigger did not create welcome notification for User A';
    assert v_other_count = 0, 'FAIL: User A was able to read User B notifications';
    insert into _test_results values (13, 'User Notifications Trigger & Isolation', 'PASS', 'User A has ' || v_own_count || ' own notification(s) and 0 from User B');
end $$;

-- 5. Final verification report
select test_num, test_name, status, detail from _test_results order by test_num;

-- Always rollback so test objects and users are never committed
rollback;

