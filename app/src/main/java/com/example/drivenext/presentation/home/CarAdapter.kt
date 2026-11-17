package com.example.drivenext.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.drivenext.R
import com.example.drivenext.domain.Car

/** Карточки автомобилей для «Главной» и «Результатов поиска». */
class CarAdapter(
    private val onBook: (Car) -> Unit,
    private val onDetails: (Car) -> Unit
) : ListAdapter<Car, CarAdapter.CarViewHolder>(DiffCallback) {

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvModel: TextView = itemView.findViewById(R.id.tvModel)
        val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvTransmission: TextView = itemView.findViewById(R.id.tvTransmission)
        val tvFuel: TextView = itemView.findViewById(R.id.tvFuel)
        val ivCar: ImageView = itemView.findViewById(R.id.ivCar)
        val btnBook: View = itemView.findViewById(R.id.btnBook)
        val btnDetails: View = itemView.findViewById(R.id.btnDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = getItem(position)

        holder.tvModel.text = car.model
        holder.tvBrand.text = car.brand
        holder.tvPrice.text = holder.itemView.context.getString(R.string.car_price, car.pricePerDay)
        holder.tvTransmission.text = car.transmission
        holder.tvFuel.text = car.fuel

        // Если у машины нет фото или оно не загрузилось, показывается картинка-заглушка
        holder.ivCar.load(car.imageUrl) {
            placeholder(R.drawable.car_placeholder)
            error(R.drawable.car_placeholder)
            fallback(R.drawable.car_placeholder)
            crossfade(true)
        }

        holder.btnBook.setOnClickListener { onBook(car) }
        holder.btnDetails.setOnClickListener { onDetails(car) }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean = oldItem == newItem
    }
}
