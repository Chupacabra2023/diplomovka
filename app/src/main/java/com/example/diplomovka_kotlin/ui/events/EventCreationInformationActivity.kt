package com.example.diplomovka_kotlin.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diplomovka_kotlin.databinding.ActivityEventCreationInformationBinding
import com.example.diplomovka_kotlin.data.models.Event
import com.google.android.material.chip.Chip
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class EventCreationInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventCreationInformationBinding
    private lateinit var event: Event

    private val allCategories = listOf(
        "Hudba", "Šport", "Party", "Kultúrne podujatia",
        "Jedlo a pitie", "Gaming", "Príroda", "Rodina",
        "Umenie", "Fotografia", "Zdravie a fitness",
        "Dobrovoľníctvo", "Workshop", "Diskusia", "Iné"
    )
    private val selectedCategories = mutableListOf<String>()
    private var dateFrom: Date? = null
    private var dateTo: Date? = null
    private val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventCreationInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        event = intent.getSerializableExtra("event") as Event
        setupCategoryChips()
        setupDatePickers()
        setupSaveButton()
        binding.etPlace.setText(event.place)
    }

    private fun setupCategoryChips() {
        allCategories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedCategories.add(category)
                    else selectedCategories.remove(category)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupDatePickers() {
        binding.etDateFrom.setOnClickListener {
            showDateTimePicker { date -> dateFrom = date; binding.etDateFrom.setText(formatter.format(date)) }
        }
        binding.etDateTo.setOnClickListener {
            showDateTimePicker { date -> dateTo = date; binding.etDateTo.setText(formatter.format(date)) }
        }
    }

    private fun showDateTimePicker(onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                cal.set(y, m, d, h, min)
                onSelected(cal.time)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Prosím, zadaj názov udalosti.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updatedEvent = event.copy(
                title = title,
                description = binding.etDescription.text.toString().trim(),
                place = binding.etPlace.text.toString().trim(),
                price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0,
                participants = binding.etParticipants.text.toString().toIntOrNull() ?: 0,
                dateFrom = dateFrom, dateTo = dateTo,
                category = if (selectedCategories.isNotEmpty())
                    selectedCategories.joinToString(", ") else "Nezaradené"
            )
            setResult(RESULT_OK, Intent().apply {
                putExtra("updatedEvent", updatedEvent as Serializable)
            })
            finish()
        }
    }
}
