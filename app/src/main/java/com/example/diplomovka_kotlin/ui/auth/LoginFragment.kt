package com.example.diplomovka_kotlin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.viewmodel.LoginViewModel
import com.example.diplomovka_kotlin.viewmodel.LoginViewModelFactory
import com.example.diplomovka_kotlin.databinding.ActivityLoginBinding
import com.example.diplomovka_kotlin.ui.map.MapActivity
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val vm: LoginViewModel by viewModels {
        LoginViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupButtons()
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            login(
                binding.etEmail.text.toString().trim(),
                binding.etPassword.text.toString().trim()
            )
        }
        binding.btnForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_resetPassword)
        }
    }

    private fun login(email: String, password: String) {
        lifecycleScope.launch {
            if (vm.login(email, password)) {
                startActivity(
                    Intent(requireContext(), MapActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
        }
    }

    private fun observeViewModel() {
        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !loading
            binding.btnForgotPassword.isEnabled = !loading
        }
        vm.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "❌ $it", Toast.LENGTH_LONG).show()
                vm.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
