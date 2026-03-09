package com.example.diplomovka_kotlin.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplomovka_kotlin.adapters.FaqAdapter
import com.example.diplomovka_kotlin.adapters.FaqItem
import com.example.diplomovka_kotlin.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding

    private val faqItems = listOf(
        FaqItem("Ako môžem zmeniť heslo?", "Choď do Nastavení a klikni na Zmeniť heslo."),
        FaqItem("Ako označím udalosť ako obľúbenú?", "Klikni na hviezdičku pri udalosti."),
        FaqItem("Ako zruším svoj účet?", "V Nastaveniach dole klikni na Odstrániť účet.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFaqList()
        setupContactButtons()
    }

    private fun setupFaqList() {
        binding.rvFaq.layoutManager = LinearLayoutManager(this)
        binding.rvFaq.adapter = FaqAdapter(faqItems)
    }

    private fun setupContactButtons() {
        binding.llEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@mojaappka.sk")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Otváram e-mailový klient...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
