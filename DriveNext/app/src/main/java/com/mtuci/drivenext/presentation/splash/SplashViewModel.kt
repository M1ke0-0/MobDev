package com.mtuci.drivenext.presentation.splash

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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface SplashEvent {
    data object NoConnection : SplashEvent
    data class Navigate(val destination: StartDestination) : SplashEvent
}

class SplashViewModel(
    private val networkRepository: NetworkRepository,
    private val getStartDestination: GetStartDestinationUseCase
) : ViewModel() {

    private val _events = Channel<SplashEvent>(Channel.BUFFERED)
    val events: Flow<SplashEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            val event = if (networkRepository.isInternetAvailable()) {
                SplashEvent.Navigate(getStartDestination())
            } else {
                SplashEvent.NoConnection
            }
            _events.send(event)
        }
    }

    companion object {
        private const val SPLASH_DURATION_MS = 2_000L

        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SplashViewModel(container.networkRepository, container.getStartDestinationUseCase)
            }
        }
    }
}
