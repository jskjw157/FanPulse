package com.aos.fanpulse.presentation.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GoogleSignInUseCase
import com.aos.fanpulse.presentation.common.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleLoginUseCase: GoogleSignInUseCase,
) : ContainerHost<LoginContract.SignInState, LoginContract.SideEffect>, ViewModel() {

    override val container: Container<LoginContract.SignInState, LoginContract.SideEffect> =
        container(initialState = LoginContract.SignInState())

    fun googleLogin(context: Context, onResult: (Boolean) -> Unit) = intent {
        // 로딩 상태(Loading)로 변경하여 UI에 스피너를 띄움 (O)
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
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            }
            .onFailure { exception ->
                Log.e("GoogleLoginDebug", "로그인 실패 상세 원인:", exception)
                reduce {
                    state.copy(
                        loginStatus = LoginState.Error(exception.message ?: "알 수 없는 오류")
                    )
                }
                // 실패 시 부수 효과로 에러 메시지 표시
                postSideEffect(LoginContract.SideEffect.ShowToast("로그인 실패: ${exception.message}"))
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
    }
}