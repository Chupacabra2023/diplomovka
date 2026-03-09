package com.example.diplomovka_kotlin.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.diplomovka_kotlin.adapters.EventAdapter
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.databinding.ActivityProfileSettingsBinding

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupProfilePhoto()
        setupFriendsList()
        setupSaveButton()
    }

    private fun setupProfilePhoto() {
        // Načítaj profilovú fotku cez Glide
        Glide.with(this)
            .load("https://via.placeholder.com/150")
            .placeholder(R.drawable.ic_person)
            .into(binding.ivProfilePhoto)

        binding.ivProfilePhoto.setOnClickListener {
            // neskôr pridaj ImagePicker
            Toast.makeText(this, "Zmena profilovej fotky ešte nefunguje", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFriendsList() {
        // placeholder - neskôr načítaj z Firebase
        binding.rvFriends.layoutManager = LinearLayoutManager(this)
        binding.rvFriends.adapter = EventAdapter(emptyList()) {}
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // neskôr ulož do Firebase
            Toast.makeText(this, "Zmeny uložené (zatím len vizuálne)", Toast.LENGTH_SHORT).show()
        }
    }
}