package com.example.diplomovka_kotlin.ui.events

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplomovka_kotlin.adapters.InvitationAdapter
import com.example.diplomovka_kotlin.adapters.InvitationItem
import com.example.diplomovka_kotlin.databinding.ActivityMyInvitationsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyInvitationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyInvitationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyInvitationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvInvitations.layoutManager = LinearLayoutManager(this)
        loadInvitations()
    }

    private fun loadInvitations() {
        lifecycleScope.launch {
            delay(1000) // simulácia - neskôr nahraď Firebase volaním

            val invitations = listOf(
                InvitationItem("Pozvánka: Firemný večierok", "Marek", "Čaká na odpoveď"),
                InvitationItem("Pozvánka: Narodeninová párty", "Sára", "Prijaté")
            )

            binding.progressBar.visibility = View.GONE
            binding.rvInvitations.visibility = View.VISIBLE
            binding.rvInvitations.adapter = InvitationAdapter(invitations) { invite, action ->
                Toast.makeText(
                    this@MyInvitationsActivity,
                    "Zvolené: $action pre ${invite.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}