package com.aos.fanpulse.presentation.login

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GoogleLoginUseCase
import com.aos.fanpulse.presentation.common.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleLoginUseCase: GoogleLoginUseCase,
) : ContainerHost<LoginContract.SignInState, LoginContract.SideEffect>, ViewModel() {

    override val container: Container<LoginContract.SignInState, LoginContract.SideEffect> =
        container(initialState = LoginContract.SignInState())

    fun googleLogin() = intent {
        // 상태를 IDLE로 초기화
        reduce {
            state.copy(
                loginStatus = LoginState.Loading
            )
        }

        // Google 로그인 유스케이스 호출
        googleLoginUseCase()
            .onSuccess { credential ->
                for (key in credential.data.keySet()) {
                    val value = credential.data.getString(key)
                }

                reduce {
                    state.copy(
                        loginStatus = LoginState.Success
                    )
                }
                // 성공 시 부수 효과로 토스트 메시지 표시
                postSideEffect(LoginContract.SideEffect.ShowToast("Login successful: $credential"))
            }
            .onFailure { exception ->
                reduce {
                    state.copy(
                        loginStatus = LoginState.Error("Login failed")
                    )
                }
                // 실패 시 부수 효과로 에러 메시지 표시
                postSideEffect(LoginContract.SideEffect.ShowToast("Login failed: ${exception.message}"))
            }
    }

    fun onUsernameChanged(username: String) = intent {
        reduce {
            state.copy(username = username)
        }
    }

    fun onPasswordChanged(password: String) = intent {
        reduce {
            state.copy(password = password)
        }
    }

    fun onPasswordVisibilityToggle() = intent {
        reduce {
            state.copy(isPassWordVisibility = !state.isPassWordVisibility)
        }
    }
}