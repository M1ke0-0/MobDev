package com.mtuci.drivenext.presentation.auth.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.R
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.usecase.GoogleSignInUseCase
import com.mtuci.drivenext.domain.usecase.ResetPasswordUseCase
import com.mtuci.drivenext.domain.usecase.SignInUseCase
import com.mtuci.drivenext.domain.validation.Validators
import com.mtuci.drivenext.presentation.common.UiText
import com.mtuci.drivenext.presentation.common.appContainer
import com.mtuci.drivenext.presentation.common.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    val isLoading: Boolean = false
) {
    // По ТЗ «Войти» активна только когда заполнены все поля
    val isSignInEnabled: Boolean
        get() = email.isNotBlank() && password.isNotEmpty() && !isLoading
}

sealed interface LoginEvent {
    data object OpenMain : LoginEvent
    data object OpenRegistration : LoginEvent
    data object OpenNoConnection : LoginEvent
    data class ShowError(val message: UiText) : LoginEvent
    data class ShowMessage(val message: UiText) : LoginEvent
}

class LoginViewModel(
    private val signIn: SignInUseCase,
    private val googleSignIn: GoogleSignInUseCase,
    private val resetPassword: ResetPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onEmailChanged(email: String) = _state.update { it.copy(email = email, emailError = null) }

    fun onPasswordChanged(password: String) = _state.update { it.copy(password = password) }

    fun onSignInClick() {
        val current = _state.value
        if (!current.isSignInEnabled) return
        if (!Validators.isValidEmail(current.email)) {
            _state.update { it.copy(emailError = R.string.error_invalid_email) }
            return
        }
        request(onSuccess = { _events.send(LoginEvent.OpenMain) }) {
            signIn(current.email, current.password)
        }
    }

    fun onGoogleClick() {
        if (_state.value.isLoading) return
        // Успех здесь — только открытие браузера; сессия придёт позже через onAuthCallback
        request(onSuccess = {}) { googleSignIn.start() }
    }

    /** Google вернул пользователя по ссылке drivenext://login-callback?code=… */
    fun onAuthCallback(url: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = googleSignIn.complete(url)
            _state.update { it.copy(isLoading = false) }
            result?.fold(
                onSuccess = { _events.send(LoginEvent.OpenMain) },
                onFailure = { handleFailure(it) }
            )
        }
    }

    fun onForgotPasswordClick() {
        val email = _state.value.email
        if (!Validators.isValidEmail(email)) {
            _state.update { it.copy(emailError = R.string.error_invalid_email) }
            viewModelScope.launch { _events.send(LoginEvent.ShowMessage(UiText(R.string.login_reset_need_email))) }
            return
        }
        request(onSuccess = { _events.send(LoginEvent.ShowMessage(UiText(R.string.login_reset_sent, email.trim()))) }) {
            resetPassword(email)
        }
    }

    fun onRegisterClick() {
        viewModelScope.launch { _events.send(LoginEvent.OpenRegistration) }
    }

    private fun request(onSuccess: suspend () -> Unit, block: suspend () -> Result<Unit>) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = block()
            _state.update { it.copy(isLoading = false) }
            result.fold(onSuccess = { onSuccess() }, onFailure = { handleFailure(it) })
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        val event = if (error is AppError.NoConnection) LoginEvent.OpenNoConnection else LoginEvent.ShowError(error.toUiText())
        _events.send(event)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                LoginViewModel(container.signInUseCase, container.googleSignInUseCase, container.resetPasswordUseCase)
            }
        }
    }
}
