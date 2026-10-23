package com.mtuci.drivenext.presentation.bookings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ItemBookingBinding
import com.mtuci.drivenext.domain.model.Booking
import com.mtuci.drivenext.presentation.booking.formatDate
import com.mtuci.drivenext.presentation.booking.formatDateTime

/** Активное бронирование: дата и время начала, по нажатию — детали. Завершённое: только дата окончания. */
class BookingAdapter(
    private val onActiveClick: (Booking) -> Unit
) : ListAdapter<Booking, BookingAdapter.BookingViewHolder>(BookingDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(private val binding: ItemBookingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            val context = binding.root.context
            binding.tvCar.text = context.getString(R.string.car_full_name, booking.car.brand, booking.car.model)
            if (booking.isActive()) {
                binding.tvDate.text = context.getString(R.string.my_bookings_active, booking.start.formatDateTime())
                binding.row.isClickable = true
                binding.row.setOnClickListener { onActiveClick(booking) }
            } else {
                binding.tvDate.text = context.getString(R.string.my_bookings_finished, booking.end.formatDate())
                binding.row.setOnClickListener(null)
                binding.row.isClickable = false
            }
        }
    }

    private object BookingDiff : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking) = oldItem == newItem
    }
}
