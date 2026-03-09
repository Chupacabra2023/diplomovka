package com.example.diplomovka_kotlin.ui.events

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplomovka_kotlin.databinding.ActivityMyCreatedEventsBinding
import com.example.diplomovka_kotlin.events.EventAdapter
import com.example.diplomovka_kotlin.events.EventItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyCreatedEventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyCreatedEventsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCreatedEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        loadEvents()
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            // simulácia načítania - neskôr nahraď Firebase volaním
            delay(1000)

            val events = listOf(
                EventItem("Letný festival", "2025-07-12"),
                EventItem("Hackathon", "2025-09-05")
            )

            binding.progressBar.visibility = View.GONE
            binding.rvEvents.visibility = View.VISIBLE
            binding.rvEvents.adapter = EventAdapter(events) { event ->
                // tu neskôr pridaj detail udalosti
            }
        }
    }
}
