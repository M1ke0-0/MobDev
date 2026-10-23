package com.mtuci.drivenext.presentation.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mtuci.drivenext.databinding.FragmentFavoritesBinding
import com.mtuci.drivenext.presentation.cars.CarAdapter
import com.mtuci.drivenext.presentation.cars.setupCarList
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.main.MainNavigator

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels { FavoritesViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navigator = requireActivity() as MainNavigator
        // Та же карточка, что на «Главной»: кнопки «Забронировать» и «Детали»
        val adapter = CarAdapter(
            onBookClick = { car -> navigator.openBooking(car) },
            onDetailsClick = { car -> navigator.openCarDetails(car.id) }
        )
        binding.rvCars.setupCarList(adapter)

        viewLifecycleOwner.collectWhenStarted(viewModel.cars) { cars ->
            adapter.submitList(cars)
            binding.tvEmpty.isVisible = cars?.isEmpty() == true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
