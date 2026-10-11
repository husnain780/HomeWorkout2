package com.homeworkout.app.events

sealed class AuthEvent {

    data class LoginRequested(
        val email: String
    ) : AuthEvent()

    data class RegisterRequested(
        val name: String,
        val email: String
    ) : AuthEvent()

    data object LoggedIn : AuthEvent()

    data object LoggedOut : AuthEvent()
}
