package com.aos.fanpulse.presentation.login

import android.content.Context
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

    fun googleLogin(context: Context) = intent {
        // 상태를 IDLE로 초기화
        reduce {
            state.copy(
                loginStatus = LoginState.Loading
            )
        }

        googleLoginUseCase(context)
            .onSuccess { credential ->
                reduce {
                    state.copy(
                        loginStatus = LoginState.Success
                    )
                }
                // 성공 시 부수 효과로 토스트 메시지 표시
                postSideEffect(LoginContract.SideEffect.ShowToast("Login successful: $credential"))
                //  성공시 mainScreen으로 가기
                //  postSideEffect(LoginContract.SideEffect.NavigateToMain)
            }
            .onFailure { exception ->
                reduce {
                    state.copy(
                        loginStatus = LoginState.Error(exception.message ?: "알 수 없는 오류")
                    )
                }
                // 실패 시 부수 효과로 에러 메시지 표시
                postSideEffect(LoginContract.SideEffect.ShowToast("로그인 실패: ${exception.message}"))
            }
    }
}