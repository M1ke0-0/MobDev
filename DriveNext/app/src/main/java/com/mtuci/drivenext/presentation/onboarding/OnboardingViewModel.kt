package com.mtuci.drivenext.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.domain.repository.OnboardingRepository
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    val pages: List<OnboardingPage> = onboardingPages

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _finished = Channel<Unit>(Channel.BUFFERED)
    val finished: Flow<Unit> = _finished.receiveAsFlow()

    fun isLastPage(position: Int): Boolean = position == pages.lastIndex

    fun onPageSelected(position: Int) {
        _currentPage.value = position
    }

    fun onNextClick() {
        val current = _currentPage.value
        if (isLastPage(current)) completeOnboarding() else _currentPage.value = current + 1
    }

    fun onSkipClick() = completeOnboarding()

    private fun completeOnboarding() {
        onboardingRepository.setOnboardingCompleted()
        _finished.trySend(Unit)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { OnboardingViewModel(appContainer().onboardingRepository) }
        }
    }
}
