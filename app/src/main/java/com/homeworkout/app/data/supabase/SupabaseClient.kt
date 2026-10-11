package com.homeworkout.app.data.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.homeworkout.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class SupabaseUser(
    val id: String,
    val email: String?,
    val displayName: String?
)

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String?,
    val user: SupabaseUser
)

object SupabaseClient {

    private const val TAG = "SupabaseClient"
    private const val PREFS_NAME = "supabase_session_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_DISPLAY_NAME = "user_display_name"
    private const val KEY_PKCE_VERIFIER = "pkce_code_verifier"

    val baseUrl: String = BuildConfig.SUPABASE_URL.trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY

    private var prefs: SharedPreferences? = null
    private var memoryPkceVerifier: String? = null
    private val authListeners = java.util.concurrent.CopyOnWriteArrayList<(SupabaseUser?) -> Unit>()

    var currentSession: SupabaseSession? = null
        private set

    val currentUser: SupabaseUser?
        get() = currentSession?.user

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSession()
        }
    }

    private fun savePkceVerifier(verifier: String) {
        memoryPkceVerifier = verifier
        prefs?.edit()?.putString(KEY_PKCE_VERIFIER, verifier)?.commit()
    }

    internal fun peekPkceVerifier(): String? {
        return prefs?.getString(KEY_PKCE_VERIFIER, null) ?: memoryPkceVerifier
    }

    private fun clearPkceVerifier() {
        memoryPkceVerifier = null
        prefs?.edit()?.remove(KEY_PKCE_VERIFIER)?.apply()
    }

    fun addAuthStateListener(listener: (SupabaseUser?) -> Unit) {
        authListeners.add(listener)
        listener(currentUser)
    }

    fun removeAuthStateListener(listener: (SupabaseUser?) -> Unit) {
        authListeners.remove(listener)
    }

    private fun notifyAuthChanged() {
        val user = currentUser
        authListeners.forEach { listener ->
            try {
                listener(user)
            } catch (e: Exception) {
                Log.e(TAG, "Error in auth listener", e)
            }
        }
    }

    fun isJwtExpired(token: String?): Boolean {
        if (token.isNullOrBlank()) return true
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true
            val decoded = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
            val json = JSONObject(String(decoded, Charsets.UTF_8))
            val exp = json.optLong("exp", 0L)
            if (exp == 0L) return false
            val nowSec = System.currentTimeMillis() / 1000
            // Proactively consider expired if within 60 seconds of expiration
            exp <= (nowSec + 60)
        } catch (e: Exception) {
            true
        }
    }

    private fun loadSession() {
        val sp = prefs ?: return
        val token = sp.getString(KEY_ACCESS_TOKEN, null) ?: ""
        val refreshToken = sp.getString(KEY_REFRESH_TOKEN, null)
        val userId = sp.getString(KEY_USER_ID, null)
        if (!userId.isNullOrBlank() && (token.isNotBlank() || !refreshToken.isNullOrBlank())) {
            val user = SupabaseUser(
                id = userId,
                email = sp.getString(KEY_USER_EMAIL, null),
                displayName = sp.getString(KEY_USER_DISPLAY_NAME, null)
            )
            currentSession = SupabaseSession(
                accessToken = token,
                refreshToken = refreshToken,
                user = user
            )
            Log.d(TAG, "Restored Supabase session for user: $userId (token expired: ${isJwtExpired(token)})")
        }
    }

    private fun saveSession(session: SupabaseSession?) {
        currentSession = session
        val sp = prefs?.edit() ?: return
        if (session != null) {
            sp.putString(KEY_ACCESS_TOKEN, session.accessToken)
            sp.putString(KEY_REFRESH_TOKEN, session.refreshToken)
            sp.putString(KEY_USER_ID, session.user.id)
            sp.putString(KEY_USER_EMAIL, session.user.email)
            sp.putString(KEY_USER_DISPLAY_NAME, session.user.displayName)
        } else {
            sp.clear()
        }
        sp.apply()
        notifyAuthChanged()
    }

    // --- AUTHENTICATION ---
    object Auth {

        suspend fun signUp(
            email: String,
            password: String,
            displayName: String? = null
        ): Result<SupabaseUser> = withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/auth/v1/signup")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val body = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password)
                    if (!displayName.isNullOrBlank()) {
                        put("data", JSONObject().put("display_name", displayName.trim()))
                    }
                }

                writeJson(conn, body)
                val status = conn.responseCode
                val responseText = readResponse(conn)

                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val userObj = json.optJSONObject("user") ?: json
                    val user = parseUser(userObj, displayName)
                    val token = json.optString("access_token").takeIf { it.isNotBlank() }
                    if (token != null) {
                        saveSession(
                            SupabaseSession(
                                accessToken = token,
                                refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                                user = user
                            )
                        )
                    }
                    Result.success(user)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val msg = errJson?.optString("msg")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("error_description")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("message")?.takeIf { it.isNotBlank() }
                        ?: "Registration failed with status $status."
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "signUp error", e)
                Result.failure(e)
            }
        }

        suspend fun signInWithPassword(
            email: String,
            password: String
        ): Result<SupabaseUser> = withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/auth/v1/token?grant_type=password")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val body = JSONObject().apply {
                    put("email", email.trim())
                    put("password", password)
                }

                writeJson(conn, body)
                val status = conn.responseCode
                val responseText = readResponse(conn)

                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val userObj = json.optJSONObject("user") ?: JSONObject()
                    val user = parseUser(userObj)
                    val token = json.optString("access_token")
                    saveSession(
                        SupabaseSession(
                            accessToken = token,
                            refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                            user = user
                        )
                    )
                    Result.success(user)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val msg = errJson?.optString("msg")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("error_description")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("message")?.takeIf { it.isNotBlank() }
                        ?: "Login failed with status $status."
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "signIn error", e)
                Result.failure(e)
            }
        }

        fun getOAuthUrl(
            provider: String = "google",
            redirectTo: String = "homeworkout://auth-callback"
        ): String {
            val verifierBytes = ByteArray(32)
            java.security.SecureRandom().nextBytes(verifierBytes)
            val codeVerifier = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes)
            savePkceVerifier(codeVerifier)

            val digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(codeVerifier.toByteArray(Charsets.US_ASCII))
            val codeChallenge = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest)

            val encodedRedirect = java.net.URLEncoder.encode(redirectTo, "UTF-8")
            return "$baseUrl/auth/v1/authorize?provider=$provider&redirect_to=$encodedRedirect&code_challenge=$codeChallenge&code_challenge_method=s256"
        }

        suspend fun handleOAuthRedirect(uri: android.net.Uri): Result<SupabaseUser> = withContext(Dispatchers.IO) {
            try {
                // 1. Check for error in query string
                val queryError = uri.getQueryParameter("error_description")
                    ?: uri.getQueryParameter("error")
                if (!queryError.isNullOrBlank()) {
                    clearPkceVerifier()
                    return@withContext Result.failure(Exception(queryError.replace('+', ' ')))
                }

                // 2. Parse fragment parameters (Implicit Grant: #access_token=...&refresh_token=...)
                val fragment = uri.fragment
                if (!fragment.isNullOrBlank()) {
                    val params = fragment.split("&").associate { param ->
                        val parts = param.split("=")
                        if (parts.size >= 2) {
                            parts[0] to runCatching { java.net.URLDecoder.decode(parts[1], "UTF-8") }.getOrDefault(parts[1])
                        } else "" to ""
                    }

                    val fragError = params["error_description"] ?: params["error"]
                    if (!fragError.isNullOrBlank()) {
                        clearPkceVerifier()
                        return@withContext Result.failure(Exception(fragError.replace('+', ' ')))
                    }

                    val accessToken = params["access_token"]
                    val refreshToken = params["refresh_token"]
                    if (!accessToken.isNullOrBlank()) {
                        clearPkceVerifier()
                        return@withContext fetchAndSaveUser(accessToken, refreshToken)
                    }
                }

                // 3. Parse PKCE code if present (?code=...)
                val code = uri.getQueryParameter("code")
                if (!code.isNullOrBlank()) {
                    return@withContext exchangeCodeForSession(code)
                }

                clearPkceVerifier()
                Result.failure(Exception("Authentication callback contained no session tokens."))
            } catch (e: Exception) {
                clearPkceVerifier()
                Log.e(TAG, "handleOAuthRedirect error", e)
                Result.failure(e)
            }
        }

        suspend fun fetchAndSaveUser(
            accessToken: String,
            refreshToken: String?
        ): Result<SupabaseUser> = withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/auth/v1/user")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Accept", "application/json")
                }
                val status = conn.responseCode
                val responseText = readResponse(conn)
                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val user = parseUser(json)
                    saveSession(
                        SupabaseSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            user = user
                        )
                    )
                    runCatching {
                        Database.upsertProfile(
                            userId = user.id,
                            displayName = user.displayName ?: user.email?.substringBefore('@') ?: "User",
                            email = user.email ?: ""
                        )
                    }
                    Result.success(user)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val msg = errJson?.optString("msg")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("error_description")?.takeIf { it.isNotBlank() }
                        ?: "Failed to load user profile ($status)."
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun exchangeCodeForSession(code: String): Result<SupabaseUser> = withContext(Dispatchers.IO) {
            try {
                val verifier = peekPkceVerifier()
                if (verifier.isNullOrBlank()) {
                    return@withContext Result.failure(
                        Exception("Authentication session expired (missing PKCE verifier). Please try signing in again.")
                    )
                }
                val url = URL("$baseUrl/auth/v1/token?grant_type=pkce")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val body = JSONObject().apply {
                    put("auth_code", code)
                    put("code_verifier", verifier)
                }
                writeJson(conn, body)
                val status = conn.responseCode
                val responseText = readResponse(conn)
                clearPkceVerifier()
                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val userObj = json.optJSONObject("user") ?: JSONObject()
                    val user = parseUser(userObj)
                    val token = json.optString("access_token")
                    saveSession(
                        SupabaseSession(
                            accessToken = token,
                            refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                            user = user
                        )
                    )
                    runCatching {
                        Database.upsertProfile(
                            userId = user.id,
                            displayName = user.displayName ?: user.email?.substringBefore('@') ?: "User",
                            email = user.email ?: ""
                        )
                    }
                    Result.success(user)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val msg = errJson?.optString("error_description")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("msg")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("message")?.takeIf { it.isNotBlank() }
                        ?: "Code exchange failed with status $status."
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                clearPkceVerifier()
                Result.failure(e)
            }
        }

        suspend fun signInWithIdToken(
            provider: String,
            idToken: String,
            nonce: String? = null
        ): Result<SupabaseUser> = withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/auth/v1/token?grant_type=id_token")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val body = JSONObject().apply {
                    put("provider", provider)
                    put("id_token", idToken)
                    if (!nonce.isNullOrBlank()) {
                        put("nonce", nonce)
                    }
                }

                writeJson(conn, body)
                val status = conn.responseCode
                val responseText = readResponse(conn)

                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val userObj = json.optJSONObject("user") ?: JSONObject()
                    val user = parseUser(userObj)
                    val token = json.optString("access_token")
                    saveSession(
                        SupabaseSession(
                            accessToken = token,
                            refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                            user = user
                        )
                    )
                    Result.success(user)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val msg = errJson?.optString("error_description")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("msg")?.takeIf { it.isNotBlank() }
                        ?: "Google sign-in failed with status $status."
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "signInWithIdToken error", e)
                Result.failure(e)
            }
        }

        fun signOut() {
            val token = currentSession?.accessToken
            saveSession(null)
            if (!token.isNullOrBlank()) {
                // Fire and forget logout on backend
                Thread {
                    try {
                        val url = URL("$baseUrl/auth/v1/logout")
                        val conn = (url.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            setRequestProperty("apikey", anonKey)
                            setRequestProperty("Authorization", "Bearer $token")
                        }
                        conn.responseCode
                    } catch (_: Exception) {}
                }.start()
            }
        }

        suspend fun refreshSession(): Result<SupabaseSession> = withContext(Dispatchers.IO) {
            val rToken = currentSession?.refreshToken
                ?: prefs?.getString(KEY_REFRESH_TOKEN, null)
                ?: return@withContext Result.failure(IllegalStateException("No refresh token available."))

            try {
                val url = URL("$baseUrl/auth/v1/token?grant_type=refresh_token")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val body = JSONObject().apply {
                    put("refresh_token", rToken)
                }

                writeJson(conn, body)
                val status = conn.responseCode
                val responseText = readResponse(conn)

                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val userObj = json.optJSONObject("user") ?: JSONObject()
                    val user = parseUser(userObj)
                    val newAccessToken = json.optString("access_token")
                    val newRefreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() } ?: rToken
                    val freshSession = SupabaseSession(
                        accessToken = newAccessToken,
                        refreshToken = newRefreshToken,
                        user = user
                    )
                    saveSession(freshSession)
                    Log.d(TAG, "Successfully refreshed Supabase session for user ${user.id}")
                    Result.success(freshSession)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val msg = errJson?.optString("error_description")?.takeIf { it.isNotBlank() }
                        ?: errJson?.optString("msg")?.takeIf { it.isNotBlank() }
                        ?: "Session refresh failed with status $status."
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "refreshSession error", e)
                Result.failure(e)
            }
        }

        private fun parseUser(userObj: JSONObject, fallbackName: String? = null): SupabaseUser {
            val id = userObj.optString("id")
            val email = userObj.optString("email").takeIf { it.isNotBlank() }
            val meta = userObj.optJSONObject("user_metadata")
            val name = meta?.optString("full_name")?.takeIf { it.isNotBlank() }
                ?: meta?.optString("name")?.takeIf { it.isNotBlank() }
                ?: meta?.optString("display_name")?.takeIf { it.isNotBlank() }
                ?: fallbackName
            return SupabaseUser(id = id, email = email, displayName = name)
        }
    }

    fun updateCurrentUserDisplayName(newDisplayName: String) {
        val session = currentSession ?: return
        val trimmed = newDisplayName.trim()
        if (trimmed.isEmpty()) return
        val updatedUser = session.user.copy(displayName = trimmed)
        val updatedSession = session.copy(user = updatedUser)
        currentSession = updatedSession
        prefs?.edit()?.putString(KEY_USER_DISPLAY_NAME, trimmed)?.apply()
        notifyAuthChanged()
    }

    // --- POSTGREST DATABASE ---
    object Database {

        suspend fun getProfile(userId: String): Result<JSONObject?> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/profiles?id=eq.$userId&select=*")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }

                val status = conn.responseCode
                val responseText = readResponse(conn)
                if (status in 200..299) {
                    val arr = JSONArray(responseText)
                    val obj = if (arr.length() > 0) arr.getJSONObject(0) else null
                    Result.success(obj)
                } else {
                    Result.failure(Exception("Get profile failed: $status - $responseText"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun upsertProfile(
            userId: String,
            displayName: String,
            email: String
        ): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/profiles")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Prefer", "resolution=merge-duplicates")
                    doOutput = true
                }

                val body = JSONObject().apply {
                    put("id", userId)
                    put("display_name", displayName)
                    put("email", email)
                    put("last_seen_at", java.time.Instant.now().toString())
                }

                writeJson(conn, body)
                val status = conn.responseCode
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    val resp = readResponse(conn)
                    Result.failure(Exception("Upsert profile failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun updateProfileDetails(
            userId: String,
            displayName: String? = null,
            email: String? = null,
            fitnessGoal: String? = null,
            preferredDifficulty: String? = null,
            weeklyGoal: Int? = null,
            notificationsEnabled: Boolean? = null,
            remindersEnabled: Boolean? = null
        ): Result<JSONObject?> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken
                    ?: return@withContext Result.failure(IllegalStateException("Not signed in."))
                val url = URL("$baseUrl/rest/v1/profiles?on_conflict=id")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation")
                    doOutput = true
                }

                val body = JSONObject().apply {
                    put("id", userId)
                    if (displayName != null) put("display_name", displayName.trim())
                    if (email != null) put("email", email.trim())
                    if (fitnessGoal != null) put("fitness_goal", fitnessGoal)
                    if (preferredDifficulty != null) put("preferred_difficulty", preferredDifficulty)
                    if (weeklyGoal != null) put("weekly_goal", weeklyGoal.coerceIn(1, 14))
                    if (notificationsEnabled != null) put("notifications_enabled", notificationsEnabled)
                    if (remindersEnabled != null) put("reminders_enabled", remindersEnabled)
                    put("last_seen_at", java.time.Instant.now().toString())
                }

                writeJson(conn, body)
                val status = conn.responseCode
                val responseText = readResponse(conn)
                if (status in 200..299) {
                    val arr = runCatching { JSONArray(responseText) }.getOrNull()
                    val obj = if (arr != null && arr.length() > 0) arr.getJSONObject(0) else null
                    Result.success(obj)
                } else {
                    Result.failure(Exception("Update profile failed: $status - $responseText"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun getWorkoutHistory(userId: String): Result<List<JSONObject>> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/workout_history?user_id=eq.$userId&order=completed_at.desc&select=*")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }

                val status = conn.responseCode
                val responseText = readResponse(conn)
                if (status in 200..299) {
                    val arr = JSONArray(responseText)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Get workout history failed: $status - $responseText"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun upsertWorkoutHistory(
            userId: String,
            id: String,
            workoutId: String,
            workoutName: String,
            durationMinutes: Int,
            exercisesCompleted: Int,
            caloriesBurned: Int,
            completedAtIso: String
        ): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/workout_history")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Prefer", "resolution=merge-duplicates")
                    doOutput = true
                }

                val body = JSONObject().apply {
                    put("id", id)
                    put("user_id", userId)
                    put("workout_id", workoutId)
                    put("workout_name", workoutName)
                    put("duration_minutes", durationMinutes)
                    put("exercises_completed", exercisesCompleted)
                    put("calories_burned", caloriesBurned)
                    put("completed_at", completedAtIso)
                }

                writeJson(conn, body)
                val status = conn.responseCode
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    val resp = readResponse(conn)
                    Result.failure(Exception("Upsert workout history failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun getSavedWorkoutPlans(userId: String): Result<List<JSONObject>> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/saved_workout_plans?user_id=eq.$userId&order=created_at.desc&select=*")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }

                val status = conn.responseCode
                val responseText = readResponse(conn)
                if (status in 200..299) {
                    val arr = JSONArray(responseText)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Get saved workout plans failed: $status - $responseText"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun upsertSavedWorkoutPlan(body: JSONObject): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/saved_workout_plans")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Prefer", "resolution=merge-duplicates")
                    doOutput = true
                }
                writeJson(conn, body)
                val status = conn.responseCode
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    val resp = readResponse(conn)
                    Result.failure(Exception("Upsert saved plan failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun deleteSavedWorkoutPlan(userId: String, planId: String): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/saved_workout_plans?user_id=eq.$userId&id=eq.$planId")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                }
                val status = conn.responseCode
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    val resp = readResponse(conn)
                    Result.failure(Exception("Delete saved plan failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun getAICoachMessages(userId: String): Result<List<JSONObject>> = withContext(Dispatchers.IO) {
            try {
                var token = currentSession?.accessToken ?: anonKey
                if (token != anonKey && isJwtExpired(token)) {
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        token = refreshed.accessToken
                    }
                }
                fun executeGet(authToken: String): Pair<Int, String> {
                    val url = URL("$baseUrl/rest/v1/ai_coach_messages?user_id=eq.$userId&order=created_at.asc&select=*")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("apikey", anonKey)
                        setRequestProperty("Authorization", "Bearer $authToken")
                        setRequestProperty("Accept", "application/json")
                    }
                    val status = conn.responseCode
                    val responseText = readResponse(conn)
                    return status to responseText
                }
                var (status, responseText) = executeGet(token)
                if (status == 401) {
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        val retry = executeGet(refreshed.accessToken)
                        status = retry.first
                        responseText = retry.second
                    }
                }
                if (status in 200..299) {
                    val arr = JSONArray(responseText)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Get AI coach messages failed: $status - $responseText"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun upsertAICoachMessage(body: JSONObject): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                var token = currentSession?.accessToken ?: anonKey
                if (token != anonKey && isJwtExpired(token)) {
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        token = refreshed.accessToken
                    }
                }
                fun executeUpsert(authToken: String): Pair<Int, String> {
                    val url = URL("$baseUrl/rest/v1/ai_coach_messages")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("apikey", anonKey)
                        setRequestProperty("Authorization", "Bearer $authToken")
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Prefer", "resolution=merge-duplicates")
                        doOutput = true
                    }
                    writeJson(conn, body)
                    val status = conn.responseCode
                    val resp = readResponse(conn)
                    return status to resp
                }
                var (status, resp) = executeUpsert(token)
                if (status == 401) {
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        val retry = executeUpsert(refreshed.accessToken)
                        status = retry.first
                        resp = retry.second
                    }
                }
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Upsert AI coach message failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun clearAICoachMessages(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                var token = currentSession?.accessToken ?: anonKey
                if (token != anonKey && isJwtExpired(token)) {
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        token = refreshed.accessToken
                    }
                }
                fun executeClear(authToken: String): Pair<Int, String> {
                    val url = URL("$baseUrl/rest/v1/ai_coach_messages?user_id=eq.$userId")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "DELETE"
                        setRequestProperty("apikey", anonKey)
                        setRequestProperty("Authorization", "Bearer $authToken")
                    }
                    val status = conn.responseCode
                    val resp = readResponse(conn)
                    return status to resp
                }
                var (status, resp) = executeClear(token)
                if (status == 401) {
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        val retry = executeClear(refreshed.accessToken)
                        status = retry.first
                        resp = retry.second
                    }
                }
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Clear AI coach messages failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun getUserNotifications(userId: String): Result<List<JSONObject>> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/user_notifications?user_id=eq.$userId&order=created_at.desc&select=*")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }
                val status = conn.responseCode
                val responseText = readResponse(conn)
                if (status in 200..299) {
                    val arr = JSONArray(responseText)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Get notifications failed: $status - $responseText"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun upsertUserNotification(body: JSONObject): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/user_notifications")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Prefer", "resolution=merge-duplicates")
                    doOutput = true
                }
                writeJson(conn, body)
                val status = conn.responseCode
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    val resp = readResponse(conn)
                    Result.failure(Exception("Upsert notification failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun clearAllUserNotifications(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/user_notifications?user_id=eq.$userId")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                }
                val status = conn.responseCode
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    val resp = readResponse(conn)
                    Result.failure(Exception("Clear notifications failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun deleteUserNotification(userId: String, id: String): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/user_notifications?user_id=eq.$userId&id=eq.$id")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                }
                val status = conn.responseCode
                if (status in 200..299) {
                    Result.success(Unit)
                } else {
                    val resp = readResponse(conn)
                    Result.failure(Exception("Delete notification failed: $status - $resp"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun getAdminStats(): Result<JSONObject> = withContext(Dispatchers.IO) {
            try {
                val token = currentSession?.accessToken ?: anonKey
                val url = URL("$baseUrl/rest/v1/rpc/get_admin_stats")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                writeJson(conn, JSONObject())
                val status = conn.responseCode
                val responseText = readResponse(conn)
                if (status in 200..299) {
                    Result.success(JSONObject(responseText))
                } else {
                    Result.failure(Exception("Get admin stats failed: $status - $responseText"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // --- EDGE FUNCTIONS ---
    object Functions {

        suspend fun callAICoach(
            message: String,
            history: List<com.homeworkout.app.model.WorkoutHistory>
        ): Result<String> = withContext(Dispatchers.IO) {
            try {
                var token = currentSession?.accessToken
                if (token.isNullOrBlank() || isJwtExpired(token)) {
                    Log.d(TAG, "callAICoach: token missing or expired, refreshing proactively...")
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        token = refreshed.accessToken
                    }
                }
                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalStateException("Please sign in to your account to use the AI Coach."))
                }

                fun executeRequest(authToken: String): Pair<Int, String> {
                    val url = URL("$baseUrl/functions/v1/ai-coach")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("apikey", anonKey)
                        setRequestProperty("Authorization", "Bearer $authToken")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        connectTimeout = 30000
                        readTimeout = 45000
                    }

                    val historyArr = JSONArray()
                    history.forEach { h ->
                        historyArr.put(
                            JSONObject().apply {
                                put("workoutName", h.workoutName)
                                put("durationMinutes", h.durationMinutes)
                                put("caloriesBurned", h.caloriesBurned)
                            }
                        )
                    }

                    val body = JSONObject().apply {
                        put("message", message)
                        put("history", historyArr)
                    }

                    writeJson(conn, body)
                    val status = conn.responseCode
                    val responseText = readResponse(conn)
                    return status to responseText
                }

                var (status, responseText) = executeRequest(token)

                if (status == 401) {
                    Log.d(TAG, "callAICoach received 401, attempting reactive token refresh...")
                    val refreshRes = Auth.refreshSession()
                    val freshToken = refreshRes.getOrNull()?.accessToken
                    if (!freshToken.isNullOrBlank() && freshToken != token) {
                        val retry = executeRequest(freshToken)
                        status = retry.first
                        responseText = retry.second
                    }
                }

                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val reply = json.optString("reply")
                    Result.success(reply)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val errMsg = errJson?.optString("error") ?: "AI Service Error ($status)"
                    Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun callAIWorkoutPlan(
            goal: String,
            difficulty: String,
            daysPerWeek: Int,
            minutesPerWorkout: Int,
            availableExercises: List<com.homeworkout.app.model.Exercise>,
            history: List<com.homeworkout.app.model.WorkoutHistory>
        ): Result<JSONObject> = withContext(Dispatchers.IO) {
            try {
                var token = currentSession?.accessToken
                if (token.isNullOrBlank() || isJwtExpired(token)) {
                    Log.d(TAG, "callAIWorkoutPlan: token missing or expired, refreshing proactively...")
                    val refreshed = Auth.refreshSession().getOrNull()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        token = refreshed.accessToken
                    }
                }
                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalStateException("Please sign in to your account to generate AI workout plans."))
                }

                fun executeRequest(authToken: String): Pair<Int, String> {
                    val url = URL("$baseUrl/functions/v1/ai-workout-plan")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("apikey", anonKey)
                        setRequestProperty("Authorization", "Bearer $authToken")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        connectTimeout = 30000
                        readTimeout = 45000
                    }

                    val exArr = JSONArray()
                    availableExercises.forEach { e ->
                        exArr.put(
                            JSONObject().apply {
                                put("id", e.id)
                                put("name", e.name)
                                put("category", e.category)
                                put("difficulty", e.difficulty)
                            }
                        )
                    }

                    val body = JSONObject().apply {
                        put("goal", goal)
                        put("difficulty", difficulty)
                        put("daysPerWeek", daysPerWeek)
                        put("minutesPerWorkout", minutesPerWorkout)
                        put("availableExercises", exArr)
                    }

                    writeJson(conn, body)
                    val status = conn.responseCode
                    val responseText = readResponse(conn)
                    return status to responseText
                }

                var (status, responseText) = executeRequest(token)

                if (status == 401) {
                    Log.d(TAG, "callAIWorkoutPlan received 401, attempting reactive token refresh...")
                    val refreshRes = Auth.refreshSession()
                    val freshToken = refreshRes.getOrNull()?.accessToken
                    if (!freshToken.isNullOrBlank() && freshToken != token) {
                        val retry = executeRequest(freshToken)
                        status = retry.first
                        responseText = retry.second
                    }
                }

                if (status in 200..299) {
                    val json = JSONObject(responseText)
                    val planJson = json.optJSONObject("plan")
                        ?: throw IllegalStateException("Plan object missing from response")
                    Result.success(planJson)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    val errMsg = errJson?.optString("error") ?: "AI Plan Error ($status)"
                    Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun writeJson(conn: HttpURLConnection, json: JSONObject) {
        OutputStreamWriter(conn.outputStream, "UTF-8").use {
            it.write(json.toString())
            it.flush()
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
    }
}
