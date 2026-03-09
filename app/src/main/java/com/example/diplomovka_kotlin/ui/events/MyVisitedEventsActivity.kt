package com.example.diplomovka_kotlin.ui.events

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplomovka_kotlin.databinding.ActivityMyVisitedEventsBinding
import com.example.diplomovka_kotlin.events.EventAdapter
import com.example.diplomovka_kotlin.events.EventItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyVisitedEventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyVisitedEventsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyVisitedEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        loadVisitedEvents()
    }

    private fun loadVisitedEvents() {
        lifecycleScope.launch {
            delay(1000) // neskôr nahraď Firebase volaním

            val events = listOf(
                EventItem("Koncert Imagine Dragons", "2025-06-01"),
                EventItem("Workshop Flutter", "2025-08-22")
            )

            binding.progressBar.visibility = View.GONE
            binding.rvEvents.visibility = View.VISIBLE
            binding.rvEvents.adapter = EventAdapter(events) { event ->
                // tu neskôr detail udalosti
            }
        }
    }
}
