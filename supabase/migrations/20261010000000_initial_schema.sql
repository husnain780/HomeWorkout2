-- Migration: 20261010000000_initial_schema.sql
-- HomeWorkout2: Hardened Supabase schema for user profiles, workout history, and admin analytics

-- 1. Profiles Table
create table if not exists public.profiles (
    id uuid references auth.users(id) on delete cascade primary key,
    display_name text,
    email text,
    role text not null default 'user' check (role in ('user', 'admin')),
    created_at timestamptz not null default now(),
    last_seen_at timestamptz not null default now()
);

create index if not exists idx_profiles_role on public.profiles(role);
alter table public.profiles enable row level security;

-- 2. Workout History Table
create table if not exists public.workout_history (
    id text not null,
    user_id uuid references auth.users(id) on delete cascade not null,
    workout_id text not null,
    workout_name text not null,
    duration_minutes integer not null default 0,
    exercises_completed integer not null default 0,
    calories_burned integer not null default 0,
    completed_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    primary key (user_id, id)
);

create index if not exists idx_workout_history_user_completed on public.workout_history(user_id, completed_at desc);
create index if not exists idx_workout_history_user_id on public.workout_history(user_id);
alter table public.workout_history enable row level security;

-- 3. Authorization Helper Function (Breaks RLS recursion, safe empty search path)
create or replace function public.is_admin()
returns boolean
language sql
security definer
set search_path = ''
stable
as $$
    select exists (
        select 1
        from public.profiles
        where id = (select auth.uid())
          and role = 'admin'
    );
$$;

revoke all on function public.is_admin() from public, anon;
grant execute on function public.is_admin() to authenticated;

-- 4. Row Level Security Policies for Profiles
drop policy if exists "Users can view own profile or admin can view all" on public.profiles;
create policy "Users can view own profile or admin can view all"
on public.profiles
for select
to authenticated
using (
    (select auth.uid()) = id or
    (select public.is_admin())
);

drop policy if exists "Users can insert own profile" on public.profiles;
create policy "Users can insert own profile"
on public.profiles
for insert
to authenticated
with check (
    (select auth.uid()) = id
);

drop policy if exists "Users can update own profile" on public.profiles;
create policy "Users can update own profile"
on public.profiles
for update
to authenticated
using (
    (select auth.uid()) = id
)
with check (
    (select auth.uid()) = id
);

-- 5. Row Level Security Policies for Workout History
drop policy if exists "Users can select own workout history" on public.workout_history;
drop policy if exists "Users can select own workout history or admin can view all" on public.workout_history;
create policy "Users can select own workout history or admin can view all"
on public.workout_history
for select
to authenticated
using (
    (select auth.uid()) = user_id or
    (select public.is_admin())
);

drop policy if exists "Users can insert own workout history" on public.workout_history;
create policy "Users can insert own workout history"
on public.workout_history
for insert
to authenticated
with check (
    (select auth.uid()) = user_id
);

drop policy if exists "Users can update own workout history" on public.workout_history;
create policy "Users can update own workout history"
on public.workout_history
for update
to authenticated
using (
    (select auth.uid()) = user_id
)
with check (
    (select auth.uid()) = user_id
);

drop policy if exists "Users can delete own workout history" on public.workout_history;
create policy "Users can delete own workout history"
on public.workout_history
for delete
to authenticated
using (
    (select auth.uid()) = user_id
);

-- 6. Privilege Escalation Prevention: Role Protection Trigger
create or replace function public.protect_profile_role()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_is_admin boolean;
begin
    if (tg_op = 'UPDATE') then
        if new.role is distinct from old.role then
            if (select auth.uid()) is not null then
                v_is_admin := (select public.is_admin());
            else
                v_is_admin := (
                    session_user in ('postgres', 'supabase_admin')
                    or coalesce(current_setting('request.jwt.claim.role', true), '') = 'service_role'
                );
            end if;

            if not coalesce(v_is_admin, false) then
                raise exception 'Unauthorized: Only administrators can modify user roles';
            end if;
        end if;
    end if;
    return new;
end;
$$;

drop trigger if exists trg_protect_profile_role on public.profiles;
create trigger trg_protect_profile_role
    before update on public.profiles
    for each row execute function public.protect_profile_role();

-- 7. Administrative Role Management Function
create or replace function public.set_user_role(target_user_id uuid, new_role text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_is_admin boolean;
begin
    if (select auth.uid()) is not null then
        v_is_admin := (select public.is_admin());
    else
        v_is_admin := (
            session_user in ('postgres', 'supabase_admin')
            or coalesce(current_setting('request.jwt.claim.role', true), '') = 'service_role'
        );
    end if;

    if not coalesce(v_is_admin, false) then
        raise exception 'Unauthorized: Only administrators can modify user roles';
    end if;

    if new_role not in ('user', 'admin') then
        raise exception 'Invalid role: %', new_role;
    end if;

    update public.profiles
    set role = new_role,
        last_seen_at = now()
    where id = target_user_id;

    if not found then
        raise exception 'User not found';
    end if;
end;
$$;

revoke all on function public.set_user_role(uuid, text) from public, anon;
grant execute on function public.set_user_role(uuid, text) to authenticated;

-- 8. Automatic Profile Creation on Signup Trigger
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (id, display_name, email, role, created_at, last_seen_at)
    values (
        new.id,
        coalesce(
            new.raw_user_meta_data->>'full_name',
            new.raw_user_meta_data->>'name',
            new.raw_user_meta_data->>'display_name',
            split_part(new.email, '@', 1)
        ),
        new.email,
        'user', -- Always assign standard user role on registration
        now(),
        now()
    )
    on conflict (id) do update set
        last_seen_at = now(),
        email = coalesce(excluded.email, public.profiles.email);
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- 9. Secure Admin Analytics RPC
create or replace function public.get_admin_stats()
returns json
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_total_users bigint;
    v_total_workouts bigint;
    v_is_admin boolean;
begin
    if not (select public.is_admin()) then
        if not (
            session_user in ('postgres', 'supabase_admin')
            or coalesce(current_setting('request.jwt.claim.role', true), '') = 'service_role'
        ) then
            raise exception 'Unauthorized: Administrator access required';
        end if;
    end if;

    select count(*) into v_total_users from public.profiles;
    select count(*) into v_total_workouts from public.workout_history;

    return json_build_object(
        'total_users', v_total_users,
        'total_cloud_workouts', v_total_workouts
    );
end;
$$;

revoke all on function public.get_admin_stats() from public, anon;
grant execute on function public.get_admin_stats() to authenticated;

-- 10. Principle of Least Privilege: Column-Level and Table Grants
grant usage on schema public to authenticated;

-- Profiles: Authenticated users can view and edit their profiles (filtered by RLS; role column protected by trg_protect_profile_role)
revoke all on public.profiles from public, anon, authenticated;
grant select, insert, update on public.profiles to authenticated;
grant update (display_name, email, last_seen_at) on public.profiles to authenticated;

-- Workout History: Authenticated users can manage their own records (filtered by RLS)
revoke all on public.workout_history from public, anon, authenticated;
grant select, insert, update, delete on public.workout_history to authenticated;
