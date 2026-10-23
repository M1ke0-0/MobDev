package com.mtuci.drivenext.presentation.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.FragmentCarDetailsBinding
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.loadCarImage
import com.mtuci.drivenext.presentation.common.openNoConnection
import com.mtuci.drivenext.presentation.main.MainNavigator

class CarDetailsFragment : Fragment() {

    private var _binding: FragmentCarDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CarDetailsViewModel by viewModels { CarDetailsViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCarDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener {
            viewModel.onFavoriteClick()
            true
        }
        binding.btnBook.setOnClickListener { viewModel.onBookClick() }
        binding.listState.btnRetry.setOnClickListener { viewModel.onRetryClick() }

        viewLifecycleOwner.collectWhenStarted(viewModel.state, ::render)
        viewLifecycleOwner.collectWhenStarted(viewModel.events, ::handleEvent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenStarted()
    }

    private fun render(state: CarDetailsUiState) {
        binding.listState.progress.isVisible = state.isLoading
        binding.listState.errorContainer.isVisible = state.isError
        binding.listState.tvError.setText(R.string.error_load_data)
        binding.content.isVisible = state.car != null && !state.isLoading && !state.isError

        val favoriteItem = binding.toolbar.menu.findItem(R.id.action_favorite)
        favoriteItem.isVisible = state.car != null
        favoriteItem.setIcon(if (state.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        favoriteItem.setTitle(if (state.isFavorite) R.string.details_remove_favorite else R.string.details_add_favorite)

        val car = state.car ?: return
        binding.tvName.text = getString(R.string.car_full_name, car.brand, car.model)
        binding.tvAddress.text = car.address
        binding.tvDescription.text = car.description ?: getString(R.string.details_no_description)
        binding.tvPrice.text = getString(R.string.car_price_per_day, car.pricePerDay)
        binding.ivCar.contentDescription = getString(R.string.car_image_description, car.brand, car.model)
        binding.ivCar.loadCarImage(car.imageUrl)
    }

    private fun handleEvent(event: CarDetailsEvent) {
        when (event) {
            is CarDetailsEvent.OpenBooking -> (requireActivity() as MainNavigator).openBooking(event.car)
            CarDetailsEvent.OpenNoConnection -> requireActivity().openNoConnection()
            CarDetailsEvent.FavoriteFailed ->
                Snackbar.make(binding.root, R.string.error_add_favorite, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(carId: String) = CarDetailsFragment().apply {
            arguments = bundleOf(CarDetailsViewModel.KEY_CAR_ID to carId)
        }
    }
}
