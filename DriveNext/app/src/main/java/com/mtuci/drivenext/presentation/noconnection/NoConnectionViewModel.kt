package com.mtuci.drivenext.presentation.noconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.domain.model.StartDestination
import com.mtuci.drivenext.domain.repository.NetworkRepository
import com.mtuci.drivenext.domain.usecase.GetStartDestinationUseCase
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface NoConnectionEvent {
    data object StillOffline : NoConnectionEvent
    data class Navigate(val destination: StartDestination) : NoConnectionEvent
}

class NoConnectionViewModel(
    private val networkRepository: NetworkRepository,
    private val getStartDestination: GetStartDestinationUseCase
) : ViewModel() {

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _events = Channel<NoConnectionEvent>(Channel.BUFFERED)
    val events: Flow<NoConnectionEvent> = _events.receiveAsFlow()

    fun onRetryClick() {
        if (_isChecking.value) return
        viewModelScope.launch {
            _isChecking.value = true
            // Проверка мгновенная — короткая пауза, чтобы пользователь увидел лоадер.
            delay(MIN_LOADER_DURATION_MS)
            val event = if (networkRepository.isInternetAvailable()) {
                NoConnectionEvent.Navigate(getStartDestination())
            } else {
                NoConnectionEvent.StillOffline
            }
            _isChecking.value = false
            _events.send(event)
        }
    }

    companion object {
        private const val MIN_LOADER_DURATION_MS = 800L

        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                NoConnectionViewModel(container.networkRepository, container.getStartDestinationUseCase)
            }
        }
    }
}
