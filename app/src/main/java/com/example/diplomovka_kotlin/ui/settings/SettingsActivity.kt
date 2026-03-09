package com.example.diplomovka_kotlin.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.diplomovka_kotlin.databinding.ActivitySettingsBinding
import com.example.diplomovka_kotlin.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var selectedLanguage = "Slovensky"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSwitches()
        setupLanguage()
        setupLogout()
    }

    private fun setupSwitches() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            Toast.makeText(
                this,
                if (isChecked) "Tmavý režim zapnutý" else "Tmavý režim vypnutý",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, _ ->
            // neskôr pridaj logiku notifikácií
        }
    }

    private fun setupLanguage() {
        binding.llLanguage.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Slovensky", "English", "Deutsch")
        val currentIndex = languages.indexOf(selectedLanguage)

        AlertDialog.Builder(this)
            .setTitle("Vyber jazyk")
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                selectedLanguage = languages[which]
                binding.tvLanguage.text = selectedLanguage
                dialog.dismiss()
            }
            .show()
    }

    private fun setupLogout() {
        binding.llLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Odhlasujem...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}