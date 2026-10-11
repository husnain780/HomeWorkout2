package com.homeworkout.app.controller

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.data.supabase.SupabaseUser
import com.homeworkout.app.events.AuthEvent
import com.homeworkout.app.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom

class AuthController(
    private val eventBus: EventBus
) {

    fun isLoggedIn(): Boolean {
        return SupabaseClient.currentUser != null
    }

    val currentUser: SupabaseUser?
        get() = SupabaseClient.currentUser

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            onResult(false, "Enter your email and password.")
            return
        }

        eventBus.publish(AuthEvent.LoginRequested(cleanEmail))

        CoroutineScope(Dispatchers.Main).launch {
            val result = SupabaseClient.Auth.signInWithPassword(cleanEmail, password)
            result.onSuccess {
                eventBus.publish(AuthEvent.LoggedIn)
                onResult(true, null)
            }.onFailure { ex ->
                val errorMsg = ex.localizedMessage ?: "Login failed."
                onResult(false, errorMsg)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        if (cleanName.isBlank()) {
            onResult(false, "Enter your name.")
            return
        }

        if (cleanEmail.isBlank()) {
            onResult(false, "Enter your email.")
            return
        }

        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters.")
            return
        }

        eventBus.publish(
            AuthEvent.RegisterRequested(
                name = cleanName,
                email = cleanEmail
            )
        )

        CoroutineScope(Dispatchers.Main).launch {
            val result = SupabaseClient.Auth.signUp(cleanEmail, password, cleanName)
            result.onSuccess { user ->
                // Ensure profile row in PostgREST public.profiles (safely)
                runCatching {
                    SupabaseClient.Database.upsertProfile(
                        userId = user.id,
                        displayName = cleanName,
                        email = cleanEmail
                    )
                }

                if (SupabaseClient.currentSession != null) {
                    eventBus.publish(AuthEvent.LoggedIn)
                    onResult(true, null)
                } else {
                    onResult(false, "Account created! Please check your email to confirm registration before logging in.")
                }
            }.onFailure { ex ->
                val errorMsg = ex.localizedMessage ?: "Registration failed."
                onResult(false, errorMsg)
            }
        }
    }

    fun loginWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onResult: (Boolean, String?) -> Unit
    ) {
        val redirectUri = "homeworkout://auth-callback"
        val oauthUrl = SupabaseClient.Auth.getOAuthUrl("google", redirectUri)

        try {
            val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(activity, android.net.Uri.parse(oauthUrl))
        } catch (e: Exception) {
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(oauthUrl)
                )
                activity.startActivity(intent)
            } catch (fallbackEx: Exception) {
                onResult(false, "Could not open browser for Google authentication: ${fallbackEx.localizedMessage}")
            }
        }
    }

    fun handleOAuthCallback(
        uri: android.net.Uri,
        onResult: (Boolean, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = SupabaseClient.Auth.handleOAuthRedirect(uri)
            result.onSuccess { user ->
                eventBus.publish(AuthEvent.LoggedIn)
                onResult(true, null)
            }.onFailure { ex ->
                val msg = ex.localizedMessage ?: "Google sign-in failed."
                val cleanMsg = when {
                    msg.contains("provider is not enabled", ignoreCase = true) ->
                        "Google Sign-In is not enabled in your Supabase project."
                    msg.contains("access_denied", ignoreCase = true) ->
                        "Google Sign-In was cancelled or access was denied."
                    msg.contains("redirect_uri_mismatch", ignoreCase = true) ->
                        "OAuth redirect URI mismatch. Check Google Cloud and Supabase settings."
                    else -> msg
                }
                onResult(false, cleanMsg)
            }
        }
    }

    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.isBlank()) {
            onResult(false, "Enter your phone number.")
            return
        }

        // Supabase Phone OTP requires an active SMS provider configured in Supabase dashboard
        onResult(
            false,
            "Supabase Phone OTP requires an active SMS provider (Twilio/MessageBird) configured in the Supabase Dashboard. Please use Email or Google sign-in."
        )
    }

    fun verifyPhoneOtp(
        otp: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        onResult(
            false,
            "Phone OTP verification is disabled until an SMS provider is configured in Supabase."
        )
    }

    fun logout() {
        SupabaseClient.Auth.signOut()
        eventBus.publish(AuthEvent.LoggedOut)
    }

    private fun generateNonce(): String {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        return MessageDigest
            .getInstance("SHA-256")
            .digest(randomBytes)
            .joinToString("") {
                "%02x".format(it)
            }
    }
}
