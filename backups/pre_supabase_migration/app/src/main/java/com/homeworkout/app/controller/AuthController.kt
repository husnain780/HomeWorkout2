package com.homeworkout.app.controller

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import android.util.Log
import com.homeworkout.app.events.AuthEvent
import com.homeworkout.app.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class AuthController(
    private val eventBus: EventBus
) {
    private val auth = FirebaseAuth.getInstance()

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

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

        auth.signInWithEmailAndPassword(
            cleanEmail,
            password
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                eventBus.publish(AuthEvent.LoggedIn)
                onResult(true, null)
            } else {
                onResult(
                    false,
                    task.exception?.localizedMessage ?: "Login failed."
                )
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

        auth.createUserWithEmailAndPassword(
            cleanEmail,
            password
        ).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onResult(
                    false,
                    task.exception?.localizedMessage ?: "Registration failed."
                )
                return@addOnCompleteListener
            }

            val user = auth.currentUser

            if (user == null) {
                onResult(
                    false,
                    "Account was created, but user session was unavailable."
                )
                return@addOnCompleteListener
            }

            val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()

            user.updateProfile(profile)
                .addOnCompleteListener {
                    eventBus.publish(AuthEvent.LoggedIn)
                    onResult(true, null)
                }
        }
    }

    fun loginWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onResult: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(activity)

                val googleOption =
                    GetSignInWithGoogleOption.Builder(
                        activity.getString(
                            com.homeworkout.app.R.string.default_web_client_id
                        )
                    )
                        .setNonce(generateNonce())
                        .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = activity,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    val firebaseCredential =
                        GoogleAuthProvider.getCredential(
                            googleCredential.idToken,
                            null
                        )

                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                eventBus.publish(AuthEvent.LoggedIn)
                                onResult(true, null)
                            } else {
                                onResult(
                                    false,
                                    task.exception?.localizedMessage
                                        ?: "Google sign-in failed."
                                )
                            }
                        }
                } else {
                    onResult(false, "Google credential was not returned.")
                }

            } catch (e: GoogleIdTokenParsingException) {
                Log.e("HomeWorkout", "Google token parsing failed", e)
                onResult(false, "Could not read Google sign-in response.")
            } catch (e: Exception) {
                Log.e("HomeWorkout", "Google sign-in failed", e)
                onResult(
                    false,
                    e.localizedMessage
                        ?: "Google sign-in was cancelled or failed."
                )
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

        val callbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(
                    credential: PhoneAuthCredential
                ) {
                    signInWithPhoneCredential(
                        credential,
                        onResult
                    )
                }

                override fun onVerificationFailed(
                    e: com.google.firebase.FirebaseException
                ) {
                    onResult(
                        false,
                        e.localizedMessage ?: "Phone verification failed."
                    )
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = id
                    resendToken = token
                    onResult(true, null)
                }
            }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(cleanPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneOtp(
        otp: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val id = verificationId

        if (id == null) {
            onResult(false, "Please request a new OTP.")
            return
        }

        if (otp.length != 6) {
            onResult(false, "Enter the 6-digit OTP.")
            return
        }

        val credential =
            PhoneAuthProvider.getCredential(
                id,
                otp
            )

        signInWithPhoneCredential(
            credential,
            onResult
        )
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    eventBus.publish(AuthEvent.LoggedIn)
                    onResult(true, null)
                } else {
                    val message =
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            "Invalid verification code."
                        } else {
                            task.exception?.localizedMessage
                                ?: "Phone login failed."
                        }

                    onResult(false, message)
                }
            }
    }

    fun logout() {
        auth.signOut()
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
