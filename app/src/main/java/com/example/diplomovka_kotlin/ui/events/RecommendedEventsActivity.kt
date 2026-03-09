package com.example.diplomovka_kotlin.ui.events

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.databinding.ActivityRecommendedEventsBinding
import com.example.diplomovka_kotlin.events.EventAdapter
import com.example.diplomovka_kotlin.events.EventItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecommendedEventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendedEventsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendedEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        loadFavoriteEvents()
    }

    private fun loadFavoriteEvents() {
        lifecycleScope.launch {
            delay(1000) // neskôr nahraď Firebase volaním

            val events = listOf(
                EventItem("Food Festival", "2025-07-10"),
                EventItem("Gaming Expo", "2025-09-18")
            )

            binding.progressBar.visibility = View.GONE
            binding.rvEvents.visibility = View.VISIBLE
            binding.rvEvents.adapter = EventAdapter(
                items = events,
                iconRes = R.drawable.ic_star,
                onItemClick = { /* detail udalosti */ }
            )
        }
    }
}
