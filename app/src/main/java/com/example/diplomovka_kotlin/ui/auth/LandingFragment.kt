package com.example.diplomovka_kotlin.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.data.services.AuthService
import com.example.diplomovka_kotlin.databinding.ActivityLandingBinding
import com.example.diplomovka_kotlin.ui.map.MapActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LandingFragment : Fragment() {

    private var _binding: ActivityLandingBinding? = null
    private val binding get() = _binding!!

    private lateinit var authService: AuthService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authService = AuthService(requireContext())
        setupButtons()
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_landing_to_login)
        }
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_landing_to_register)
        }
        binding.btnGoogle.setOnClickListener {
            googleSignInLauncher.launch(authService.getGoogleSignInIntent())
        }
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(requireContext(), "Facebook login coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.btnApple.setOnClickListener {
            Toast.makeText(requireContext(), "Apple login coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                binding.progressBar.visibility = View.VISIBLE
                binding.btnGoogle.isEnabled = false
                lifecycleScope.launch {
                    try {
                        val user = authService.firebaseAuthWithGoogle(account.idToken!!)
                        if (user != null) {
                            startActivity(
                                Intent(requireContext(), MapActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "❌ ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        _binding?.progressBar?.visibility = View.GONE
                        _binding?.btnGoogle?.isEnabled = true
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "❌ Google Sign-In zlyhal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
