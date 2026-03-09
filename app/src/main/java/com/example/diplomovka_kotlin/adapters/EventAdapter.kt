package com.example.diplomovka_kotlin.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.databinding.ActivityAuthBinding
import com.example.diplomovka_kotlin.databinding.ItemEventBinding

data class EventItem(val title: String, val date: String)

class EventAdapter(
    private val items: List<EventItem>,
    private val iconRes: Int = R.drawable.ic_event, // ← default ikona
    private val onItemClick: (EventItem) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvDate.text = "Dátum: ${item.date}"
        holder.binding.ivIcon.setImageResource(iconRes) // ← nastav ikonu
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}

class uthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // NavHostFragment v activity_auth.xml sa stará o všetky auth fragmenty
    }
}