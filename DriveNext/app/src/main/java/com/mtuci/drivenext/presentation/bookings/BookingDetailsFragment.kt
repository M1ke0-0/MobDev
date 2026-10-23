package com.mtuci.drivenext.presentation.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.FragmentBookingDetailsBinding
import com.mtuci.drivenext.presentation.booking.bind
import com.mtuci.drivenext.presentation.booking.bindPriceRows
import com.mtuci.drivenext.presentation.booking.formatDateTime
import com.mtuci.drivenext.presentation.common.collectWhenStarted

class BookingDetailsFragment : Fragment() {

    private var _binding: FragmentBookingDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingDetailsViewModel by viewModels { BookingDetailsViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.btnCancel.setOnClickListener { viewModel.onCancelClick() }

        viewLifecycleOwner.collectWhenStarted(viewModel.state, ::render)
        viewLifecycleOwner.collectWhenStarted(viewModel.events) { event ->
            when (event) {
                BookingDetailsEvent.Close -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun render(state: BookingDetailsUiState) {
        if (state.isLoading) return
        val booking = state.booking
        if (booking == null) {
            binding.toolbar.setTitle(R.string.booking_not_found)
            return
        }
        val active = booking.isActive()
        val noValue = getString(R.string.no_value)
        binding.toolbar.title = getString(R.string.booking_details_title, booking.id)
        binding.content.isVisible = true
        binding.carCard.bind(booking.car)
        binding.rowAddress.bind(getString(R.string.details_address), booking.car.address)
        binding.rowStart.bind(getString(R.string.booking_start), booking.start.formatDateTime())
        binding.rowEnd.bind(getString(R.string.booking_end), booking.end.formatDateTime())
        binding.rowDriver.bind(getString(R.string.booking_details_driver), booking.driverName.ifBlank { noValue })
        binding.rowLicense.bind(getString(R.string.booking_details_license), booking.licenseNumber.ifBlank { noValue })
        binding.rowStatus.bind(
            getString(R.string.booking_details_status),
            getString(if (active) R.string.booking_status_approved else R.string.booking_status_finished)
        )
        requireContext().bindPriceRows(booking.price, binding.rowRent, binding.rowInsurance)
        binding.tvTotal.text = getString(R.string.price_rub, booking.price.total)
        // Отменить можно только активное бронирование
        binding.btnCancel.isVisible = active
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(bookingId: Long) = BookingDetailsFragment().apply {
            arguments = bundleOf(BookingDetailsViewModel.KEY_BOOKING_ID to bookingId)
        }
    }
}
