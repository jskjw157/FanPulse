package com.aos.fanpulse.presentation.login

import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleLoginUseCase: LoginWithGoogleUseCase,
) : ContainerHost<LoginContract.SignInState, LoginContract.SideEffect>, ViewModel() {

    override val container: Container<LoginContract.SignInState, LoginContract.SideEffect> =
        container(initialState = LoginContract.SignInState())
    
    fun googleLogin(token: String, onResult: (Boolean) -> Unit) = intent {
        reduce {
            state.copy(
                loginStatus = LoginState.Loading
            )
        }

        googleLoginUseCase(token)
            .onSuccess { credential ->
                reduce {
                    state.copy(
                        loginStatus = LoginState.Success
                    )
                }
                postSideEffect(LoginContract.SideEffect.ShowToast("Login successful: $credential"))
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            }
            .onFailure { exception ->
                reduce {
                    state.copy(
                        loginStatus = LoginState.Error(exception.message ?: "알 수 없는 오류")
                    )
                }
                postSideEffect(LoginContract.SideEffect.ShowToast("로그인 실패: ${exception.message}"))
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
    }
}