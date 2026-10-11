-- Migration: 20261010020000_thirty_day_program_schema.sql
-- HomeWorkout2: Extend public.saved_workout_plans for 30-day program schedule & progress tracking

alter table public.saved_workout_plans
    add column if not exists start_date text not null default '',
    add column if not exists current_day integer not null default 1 check (current_day between 1 and 30),
    add column if not exists completed_days jsonb not null default '[]'::jsonb;

-- Ensure authenticated users retain full CRUD access to their own plans under existing RLS policies
grant select, insert, update, delete on public.saved_workout_plans to authenticated;
