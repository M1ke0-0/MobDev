package com.mtuci.drivenext.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.R
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.model.UserProfile
import com.mtuci.drivenext.domain.usecase.GetProfileUseCase
import com.mtuci.drivenext.domain.usecase.ResetPasswordUseCase
import com.mtuci.drivenext.domain.usecase.SignOutUseCase
import com.mtuci.drivenext.domain.usecase.UpdateAvatarUseCase
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

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    /** Идёт загрузка нового аватара — показываем «Сохраняем…». */
    val isSaving: Boolean = false
)

sealed interface ProfileEvent {
    data object OpenLogin : ProfileEvent
    data object OpenNoConnection : ProfileEvent
    data class ShowMessage(val message: UiText) : ProfileEvent
    data class ShowError(val message: UiText) : ProfileEvent
}

class ProfileViewModel(
    private val getProfile: GetProfileUseCase,
    private val updateAvatar: UpdateAvatarUseCase,
    private val resetPassword: ResetPasswordUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    private var retryWhenVisible = false

    init {
        viewModelScope.launch {
            getProfile.profile.collect { profile -> _state.update { it.copy(profile = profile) } }
        }
        // Профиль уже загружен «Настройками» — показываем его сразу, с сервера берём только если его нет
        if (getProfile.profile.value == null) refresh()
    }

    fun onScreenStarted() {
        if (retryWhenVisible) refresh()
    }

    /** Пользователь выбрал новое фото (null — закрыл галерею, ничего не выбрав). */
    fun onAvatarPicked(uri: String?) {
        if (uri == null || isBusy()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = updateAvatar(uri)
            _state.update { it.copy(isSaving = false) }
            result.fold(
                onSuccess = { _events.send(ProfileEvent.ShowMessage(UiText(R.string.profile_avatar_updated))) },
                onFailure = { handleFailure(it) }
            )
        }
    }

    /** «Поменять пароль»: Supabase присылает на почту ссылку для смены пароля. */
    fun onChangePasswordClick() {
        val email = _state.value.profile?.email ?: return
        if (isBusy()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = resetPassword(email)
            _state.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { _events.send(ProfileEvent.ShowMessage(UiText(R.string.profile_password_sent, email))) },
                onFailure = { handleFailure(it) }
            )
        }
    }

    fun onSignOutClick() {
        if (isBusy()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            signOut()
            _events.send(ProfileEvent.OpenLogin)
        }
    }

    private fun refresh() {
        retryWhenVisible = false
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getProfile.refresh()
            _state.update { it.copy(isLoading = false) }
            result.onFailure { handleFailure(it) }
        }
    }

    private fun isBusy() = _state.value.isLoading || _state.value.isSaving

    private suspend fun handleFailure(error: Throwable) {
        when (error) {
            is AppError.NoConnection -> {
                retryWhenVisible = _state.value.profile == null
                _events.send(ProfileEvent.OpenNoConnection)
            }
            is AppError.SessionExpired -> {
                signOut()
                _events.send(ProfileEvent.OpenLogin)
            }
            else -> _events.send(ProfileEvent.ShowError(error.toUiText()))
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ProfileViewModel(
                    container.getProfileUseCase,
                    container.updateAvatarUseCase,
                    container.resetPasswordUseCase,
                    container.signOutUseCase
                )
            }
        }
    }
}
