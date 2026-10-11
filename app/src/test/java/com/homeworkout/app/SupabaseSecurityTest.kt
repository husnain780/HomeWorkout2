package com.homeworkout.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SupabaseSecurityTest {

    private fun findMigrationFile(): File {
        val candidates = listOf(
            File("supabase/migrations/20261010000000_initial_schema.sql"),
            File("../supabase/migrations/20261010000000_initial_schema.sql"),
            File("C:/Users/husnain_fazal/AndroidStudioProjects/HomeWorkout2/supabase/migrations/20261010000000_initial_schema.sql")
        )
        return candidates.firstOrNull { it.exists() }
            ?: throw IllegalStateException("Migration file not found in candidates: $candidates")
    }

    @Test
    fun testMigrationHasNoRecursiveProfileRLS() {
        val content = findMigrationFile().readText()

        // 1. Must define security definer is_admin helper with safe search path
        assertTrue("is_admin() must be defined", content.contains("create or replace function public.is_admin()"))
        assertTrue("is_admin() must be security definer", content.contains("security definer"))
        assertTrue("is_admin() must set empty search_path", content.contains("set search_path = ''"))

        // 2. Select policy on profiles must use is_admin() helper and NOT a direct self-select loop
        assertFalse(
            "Must not contain recursive self-select in profiles policy",
            content.contains("exists (\n        select 1 from public.profiles\n        where id = (select auth.uid()) and role = 'admin'\n    )")
        )
        assertTrue(
            "Profiles policy must reference is_admin helper",
            content.contains("(select public.is_admin())")
        )
    }

    @Test
    fun testMigrationPreventsPrivilegeEscalation() {
        val content = findMigrationFile().readText()

        // 1. Column-level grants must NOT grant update on role to authenticated
        assertFalse(
            "Role column must not be granted for update to authenticated",
            content.contains("grant update (role")
        )
        assertTrue(
            "Only unprivileged columns can be updated",
            content.contains("grant update (display_name, email, last_seen_at) on public.profiles to authenticated;")
        )

        // 2. Trigger protect_profile_role must exist
        assertTrue(
            "protect_profile_role function must exist",
            content.contains("create or replace function public.protect_profile_role()")
        )
        assertTrue(
            "trg_protect_profile_role trigger must exist",
            content.contains("create trigger trg_protect_profile_role")
        )

        // 3. Signup trigger must set role = 'user'
        assertTrue(
            "handle_new_user must assign 'user' role",
            content.contains("'user'")
        )

        // 4. Safe admin assignment function must exist
        assertTrue(
            "set_user_role function must exist",
            content.contains("create or replace function public.set_user_role(")
        )
    }

    @Test
    fun testGetAdminStatsSecurity() {
        val content = findMigrationFile().readText()

        assertTrue(
            "get_admin_stats must check is_admin()",
            content.contains("if not (select public.is_admin()) then")
        )
        assertTrue(
            "get_admin_stats must revoke from public and anon",
            content.contains("revoke all on function public.get_admin_stats() from public, anon;")
        )
    }

    @Test
    fun testNoManualMigrationHistoryBookkeepingInMigrationFile() {
        val content = findMigrationFile().readText()

        assertFalse(
            "Migration file must not create schema supabase_migrations",
            content.contains("create schema if not exists supabase_migrations;")
        )
        assertFalse(
            "Migration file must not insert into schema_migrations directly",
            content.contains("insert into supabase_migrations.schema_migrations")
        )
    }

    @Test
    fun testWorkoutHistoryRLSIsolation() {
        val content = findMigrationFile().readText()

        assertTrue("RLS must be enabled on workout_history", content.contains("alter table public.workout_history enable row level security;"))
        assertTrue("Workout history must enforce user_id match on insert", content.contains("(select auth.uid()) = user_id"))
        assertTrue("Workout history select must allow admin or owner", content.contains("(select auth.uid()) = user_id or\n    (select public.is_admin())"))
    }
}
