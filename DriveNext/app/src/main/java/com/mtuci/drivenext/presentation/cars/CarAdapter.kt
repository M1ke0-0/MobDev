package com.mtuci.drivenext.presentation.cars

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ItemCarBinding
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.model.FuelType
import com.mtuci.drivenext.domain.model.Transmission
import com.mtuci.drivenext.presentation.common.loadCarImage

/**
 * Адаптер списка машин для «Главной» и «Результатов поиска».
 * ListAdapter сам считает getItemCount и через DiffUtil обновляет только изменившиеся карточки.
 */
class CarAdapter(
    private val onBookClick: (Car) -> Unit,
    private val onDetailsClick: (Car) -> Unit
) : ListAdapter<Car, CarAdapter.CarViewHolder>(CarDiff) {

    // Создаёт View карточки (вызывается всего несколько раз — потом карточки переиспользуются)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    // Заполняет карточку данными машины на позиции position
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** Хранит ссылки на элементы карточки, чтобы не искать их при каждой прокрутке. */
    inner class CarViewHolder(private val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(car: Car) {
            val context = binding.root.context
            binding.tvModel.text = car.model
            binding.tvBrand.text = car.brand
            binding.tvPrice.text = context.getString(R.string.car_price, car.pricePerDay)
            binding.tvTransmission.setText(car.transmission.label())
            binding.tvFuel.setText(car.fuelType.label())
            binding.ivCar.contentDescription = context.getString(R.string.car_image_description, car.brand, car.model)
            binding.ivCar.loadCarImage(car.imageUrl)
            binding.btnBook.setOnClickListener { onBookClick(car) }
            binding.btnDetails.setOnClickListener { onDetailsClick(car) }
        }
    }

    private fun Transmission.label(): Int = when (this) {
        Transmission.AUTOMATIC -> R.string.transmission_automatic
        Transmission.MANUAL -> R.string.transmission_manual
    }

    private fun FuelType.label(): Int = when (this) {
        FuelType.PETROL -> R.string.fuel_petrol
        FuelType.DIESEL -> R.string.fuel_diesel
        FuelType.ELECTRIC -> R.string.fuel_electric
        FuelType.HYBRID -> R.string.fuel_hybrid
    }

    private object CarDiff : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Car, newItem: Car) = oldItem == newItem
    }
}
