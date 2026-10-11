-- Migration: 20261010010000_user_features_schema.sql
-- HomeWorkout2: User preferences, saved AI workout plans, AI coach chat history, and real user notifications

-- 1. Extend public.profiles with user preferences and notification settings
alter table public.profiles
    add column if not exists fitness_goal text not null default 'Full Body',
    add column if not exists preferred_difficulty text not null default 'Beginner',
    add column if not exists weekly_goal integer not null default 4 check (weekly_goal between 1 and 14),
    add column if not exists notifications_enabled boolean not null default true,
    add column if not exists reminders_enabled boolean not null default true;

grant update (
    display_name,
    email,
    last_seen_at,
    fitness_goal,
    preferred_difficulty,
    weekly_goal,
    notifications_enabled,
    reminders_enabled
) on public.profiles to authenticated;

-- 2. Saved Workout Plans Table (stores user-saved AI and custom workout plans)
create table if not exists public.saved_workout_plans (
    id text not null,
    user_id uuid references auth.users(id) on delete cascade not null,
    title text not null,
    description text not null default '',
    goal text not null default 'Full Body',
    difficulty text not null default 'Beginner',
    days_per_week integer not null default 3,
    minutes_per_workout integer not null default 20,
    exercise_ids jsonb not null default '[]'::jsonb,
    days_json jsonb not null default '[]'::jsonb,
    generated_by_ai boolean not null default true,
    created_at timestamptz not null default now(),
    primary key (user_id, id)
);

create index if not exists idx_saved_workout_plans_user_created
    on public.saved_workout_plans(user_id, created_at desc);

alter table public.saved_workout_plans enable row level security;

drop policy if exists "Users can view own saved workout plans" on public.saved_workout_plans;
create policy "Users can view own saved workout plans"
on public.saved_workout_plans
for select
to authenticated
using ((select auth.uid()) = user_id);

drop policy if exists "Users can insert own saved workout plans" on public.saved_workout_plans;
create policy "Users can insert own saved workout plans"
on public.saved_workout_plans
for insert
to authenticated
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can update own saved workout plans" on public.saved_workout_plans;
create policy "Users can update own saved workout plans"
on public.saved_workout_plans
for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can delete own saved workout plans" on public.saved_workout_plans;
create policy "Users can delete own saved workout plans"
on public.saved_workout_plans
for delete
to authenticated
using ((select auth.uid()) = user_id);

revoke all on public.saved_workout_plans from public, anon, authenticated;
grant select, insert, update, delete on public.saved_workout_plans to authenticated;

-- 3. AI Coach Messages Table (stores persistent user-scoped AI Coach conversation history)
create table if not exists public.ai_coach_messages (
    id text not null,
    user_id uuid references auth.users(id) on delete cascade not null,
    message_text text not null,
    is_user boolean not null default true,
    created_at timestamptz not null default now(),
    primary key (user_id, id)
);

create index if not exists idx_ai_coach_messages_user_created
    on public.ai_coach_messages(user_id, created_at asc);

alter table public.ai_coach_messages enable row level security;

drop policy if exists "Users can view own ai coach messages" on public.ai_coach_messages;
create policy "Users can view own ai coach messages"
on public.ai_coach_messages
for select
to authenticated
using ((select auth.uid()) = user_id);

drop policy if exists "Users can insert own ai coach messages" on public.ai_coach_messages;
create policy "Users can insert own ai coach messages"
on public.ai_coach_messages
for insert
to authenticated
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can update own ai coach messages" on public.ai_coach_messages;
create policy "Users can update own ai coach messages"
on public.ai_coach_messages
for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can delete own ai coach messages" on public.ai_coach_messages;
create policy "Users can delete own ai coach messages"
on public.ai_coach_messages
for delete
to authenticated
using ((select auth.uid()) = user_id);

revoke all on public.ai_coach_messages from public, anon, authenticated;
grant select, insert, update, delete on public.ai_coach_messages to authenticated;

-- 4. User Notifications Table (stores real event-driven notifications per user)
create table if not exists public.user_notifications (
    id text not null,
    user_id uuid references auth.users(id) on delete cascade not null,
    title text not null,
    message text not null,
    category text not null default 'general',
    is_read boolean not null default false,
    created_at timestamptz not null default now(),
    primary key (user_id, id)
);

create index if not exists idx_user_notifications_user_created
    on public.user_notifications(user_id, created_at desc);

alter table public.user_notifications enable row level security;

drop policy if exists "Users can view own notifications" on public.user_notifications;
create policy "Users can view own notifications"
on public.user_notifications
for select
to authenticated
using ((select auth.uid()) = user_id);

drop policy if exists "Users can insert own notifications" on public.user_notifications;
create policy "Users can insert own notifications"
on public.user_notifications
for insert
to authenticated
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can update own notifications" on public.user_notifications;
create policy "Users can update own notifications"
on public.user_notifications
for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can delete own notifications" on public.user_notifications;
create policy "Users can delete own notifications"
on public.user_notifications
for delete
to authenticated
using ((select auth.uid()) = user_id);

revoke all on public.user_notifications from public, anon, authenticated;
grant select, insert, update, delete on public.user_notifications to authenticated;

-- 5. Update handle_new_user() to also create a genuine welcome notification on signup
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
        'user',
        now(),
        now()
    )
    on conflict (id) do update set
        last_seen_at = now(),
        email = coalesce(excluded.email, public.profiles.email);

    insert into public.user_notifications (id, user_id, title, message, category, is_read, created_at)
    values (
        'welcome_' || new.id::text,
        new.id,
        'Account Ready',
        'Welcome to HomeWorkout! Complete your first session or generate an AI workout plan to begin tracking real stats.',
        'account',
        false,
        now()
    )
    on conflict (user_id, id) do nothing;

    return new;
end;
$$;
