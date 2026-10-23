package com.mtuci.drivenext.presentation.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.FragmentBookingBinding
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.main.MainNavigator

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingViewModel by viewModels { BookingViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.datesBlock.setOnClickListener { showDatePicker() }
        binding.btnContinue.setOnClickListener { viewModel.onContinueClick() }

        // После поворота открытый календарь пересоздаётся сам — заново вешаем слушатель
        (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<*>)?.let(::listenDates)

        viewLifecycleOwner.collectWhenStarted(viewModel.state, ::render)
        viewLifecycleOwner.collectWhenStarted(viewModel.events) { event ->
            when (event) {
                BookingEvent.OpenSuccess -> (requireActivity() as MainNavigator).openBookingSuccess()
            }
        }
    }

    private fun render(state: BookingUiState) {
        binding.carCard.bind(state.car)
        binding.rowStart.bind(getString(R.string.booking_start), state.start.formatDateTime())
        binding.rowEnd.bind(getString(R.string.booking_end), state.end.formatDateTime())
        binding.tvAddress.text = state.car.address
        requireContext().bindPriceRows(state.price, binding.rowRent, binding.rowInsurance)
        binding.rowTotal.bind(getString(R.string.booking_total), getString(R.string.price_rub, state.price.total))
        binding.rowDeposit.bind(getString(R.string.booking_deposit), getString(R.string.price_rub, state.price.deposit))
    }

    /** Календарь выбора периода: даты раньше сегодняшней недоступны. */
    private fun showDatePicker() {
        if (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.booking_pick_dates)
            .setSelection(Pair(viewModel.selectedStartDay * DAY_MS, viewModel.selectedEndDay * DAY_MS))
            .setCalendarConstraints(
                CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build()
            )
            .build()
        listenDates(picker)
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    @Suppress("UNCHECKED_CAST")
    private fun listenDates(picker: MaterialDatePicker<*>) {
        (picker as MaterialDatePicker<Pair<Long, Long>>).addOnPositiveButtonClickListener { range ->
            // Календарь отдаёт полночь по UTC в миллисекундах — переводим в номер дня
            viewModel.onDatesSelected(range.first / DAY_MS, range.second / DAY_MS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DATE_PICKER_TAG = "booking_dates"
        private const val DAY_MS = 24 * 60 * 60 * 1000L

        fun newInstance(car: Car) = BookingFragment().apply {
            arguments = bundleOf(BookingViewModel.KEY_CAR to car)
        }
    }
}
