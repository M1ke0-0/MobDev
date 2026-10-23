package com.mtuci.drivenext.presentation.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.R
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.model.UserProfile
import com.mtuci.drivenext.domain.usecase.GetProfileUseCase
import com.mtuci.drivenext.domain.usecase.SignOutUseCase
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

data class SettingsUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false
)

enum class SettingsItem {
    BOOKINGS,
    THEME,
    NOTIFICATIONS,
    ADD_CAR,
    HELP,
    INVITE
}

sealed interface SettingsEvent {
    data object OpenProfile : SettingsEvent
    data object OpenMyBookings : SettingsEvent
    data class OpenComingSoon(@StringRes val titleRes: Int) : SettingsEvent
    data object ShareInvite : SettingsEvent
    data object OpenNoConnection : SettingsEvent
    data object OpenLogin : SettingsEvent
    data class ShowMessage(val message: UiText) : SettingsEvent
    data class ShowError(val message: UiText) : SettingsEvent
}

class SettingsViewModel(
    private val getProfile: GetProfileUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    private var retryWhenVisible = false

    init {
        // Профиль общий с экраном «Профиль»: сменили там аватар — он сразу обновится и здесь
        viewModelScope.launch {
            getProfile.profile.collect { profile -> _state.update { it.copy(profile = profile) } }
        }
        refresh()
    }

    fun onScreenStarted() {
        if (retryWhenVisible) refresh()
    }

    fun onProfileClick() = send(SettingsEvent.OpenProfile)

    fun onItemClick(item: SettingsItem) = send(
        when (item) {
            SettingsItem.BOOKINGS -> SettingsEvent.OpenMyBookings
            SettingsItem.ADD_CAR -> SettingsEvent.OpenComingSoon(R.string.add_car_title)
            SettingsItem.INVITE -> SettingsEvent.ShareInvite
            SettingsItem.THEME, SettingsItem.NOTIFICATIONS, SettingsItem.HELP ->
                SettingsEvent.ShowMessage(UiText(R.string.settings_in_progress))
        }
    )

    private fun refresh() {
        retryWhenVisible = false
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getProfile.refresh()
            _state.update { it.copy(isLoading = false) }
            result.onFailure { handleFailure(it) }
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        when (error) {
            is AppError.NoConnection -> {
                retryWhenVisible = true
                _events.send(SettingsEvent.OpenNoConnection)
            }
            // Сессии нет (например, удалили пользователя) — выходим и отправляем на вход
            is AppError.SessionExpired -> {
                signOut()
                _events.send(SettingsEvent.OpenLogin)
            }
            else -> _events.send(SettingsEvent.ShowError(error.toUiText()))
        }
    }

    private fun send(event: SettingsEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SettingsViewModel(container.getProfileUseCase, container.signOutUseCase)
            }
        }
    }
}
