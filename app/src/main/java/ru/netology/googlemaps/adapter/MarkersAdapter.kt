package ru.netology.googlemaps.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.Marker
import ru.netology.googlemaps.databinding.CardMarkerBinding


class MarkersAdapter() :
    ListAdapter<Marker, MarkerViewHolder>(MarkerDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val binding = CardMarkerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MarkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class MarkerViewHolder(
    private val binding: CardMarkerBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(marker: Marker) {
        binding.apply {
            title.text = marker.title
            latitude.text = String.format("%.4f", marker.position.latitude)
            longitude.text = String.format("%.4f", marker.position.longitude)
        }
    }
}


class MarkerDiffCallback : DiffUtil.ItemCallback<Marker>() {
    override fun areItemsTheSame(oldItem: Marker, newItem: Marker): Boolean {
        if (oldItem.javaClass != newItem.javaClass) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Marker, newItem: Marker): Boolean {
        return oldItem == newItem
    }
}
