package com.mtuci.drivenext.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.repository.FavoritesRepository
import com.mtuci.drivenext.presentation.common.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(favorites: FavoritesRepository) : ViewModel() {

    /** null — список ещё читается из базы. */
    val cars: StateFlow<List<Car>?> = favorites.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { FavoritesViewModel(appContainer().favoritesRepository) }
        }
    }
}
