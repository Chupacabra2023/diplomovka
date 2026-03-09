package com.example.diplomovka_kotlin.ui.events

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.diplomovka_kotlin.databinding.ActivityEventDetailBinding
import com.example.diplomovka_kotlin.data.models.Event
import java.text.SimpleDateFormat
import java.util.*

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var event: Event
    private val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updated = result.data?.getSerializableExtra("updatedEvent") as? Event
            updated?.let { event = it; bindEvent() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        event = intent.getSerializableExtra("event") as Event
        bindEvent()
        binding.btnEdit.setOnClickListener {
            editLauncher.launch(
                Intent(this, EventCreationInformationActivity::class.java).apply {
                    putExtra("event", event)
                }
            )
        }
    }

    private fun bindEvent() {
        val e = event
        binding.tvId.text = "🆔 ID: ${e.id}"
        binding.tvPlace.text = "📍 Miesto: ${e.place}"
        binding.tvCoords.text = "🌍 Poloha: ${e.latitude}, ${e.longitude}"
        binding.tvDescription.text = "📝 Popis: ${e.description.ifEmpty { "—" }}"
        binding.tvCreatedAt.text = "📅 Vytvorené: ${formatter.format(e.createdAt)}"
        e.dateFrom?.let { binding.tvDateFrom.visibility = View.VISIBLE; binding.tvDateFrom.text = "⏰ Od: ${formatter.format(it)}" }
        e.dateTo?.let { binding.tvDateTo.visibility = View.VISIBLE; binding.tvDateTo.text = "⏰ Do: ${formatter.format(it)}" }
        binding.tvPrice.text = "💰 Cena: ${"%.2f".format(e.price)} €"
        binding.tvParticipants.text = "👥 Účastníci: ${e.participants}"
        binding.tvVisibility.text = "👁️ Viditeľnosť: ${e.visibility}"
        binding.tvCategory.text = "🏷️ Kategória: ${e.category.ifEmpty { "—" }}"
        supportActionBar?.title = e.title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
