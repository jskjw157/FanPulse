package com.aos.fanpulse.presentation.login

object LoginContract {
    data class SignInState(
        val username: String = "",
        val email: String = "",
        val loginStatus: LoginState = LoginState.Idle,
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateHome : SideEffect
    }
}