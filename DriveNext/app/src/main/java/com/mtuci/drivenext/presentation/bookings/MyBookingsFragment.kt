package com.mtuci.drivenext.presentation.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mtuci.drivenext.databinding.FragmentMyBookingsBinding
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.main.MainNavigator

class MyBookingsFragment : Fragment() {

    private var _binding: FragmentMyBookingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyBookingsViewModel by viewModels { MyBookingsViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        val adapter = BookingAdapter { booking -> (requireActivity() as MainNavigator).openBookingDetails(booking.id) }
        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = adapter

        viewLifecycleOwner.collectWhenStarted(viewModel.bookings) { bookings ->
            adapter.submitList(bookings)
            binding.tvEmpty.isVisible = bookings?.isEmpty() == true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
