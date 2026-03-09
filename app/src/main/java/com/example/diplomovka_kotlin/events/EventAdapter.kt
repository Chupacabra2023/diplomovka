package com.example.diplomovka_kotlin.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.databinding.ItemEventBinding

data class EventItem(val title: String, val date: String)

class EventAdapter(
    private val items: List<EventItem>,
    private val iconRes: Int = R.drawable.ic_event,
    private val onItemClick: (EventItem) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EventViewHolder(ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvDate.text = "Dátum: ${item.date}"
        holder.binding.ivIcon.setImageResource(iconRes)
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
