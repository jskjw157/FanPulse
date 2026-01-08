package com.aos.fanpulse.presentation.login

import com.aos.fanpulse.presentation.common.LoginState

object LoginContract {
    data class SignInState(
        val username: String = "",
        val password: String = "",
        val loginStatus: LoginState = LoginState.Idle,
        val isPassWordVisibility: Boolean = false
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateHome : SideEffect
    }
}