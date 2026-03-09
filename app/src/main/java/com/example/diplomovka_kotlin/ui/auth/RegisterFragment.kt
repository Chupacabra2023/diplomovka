package com.example.diplomovka_kotlin.ui.auth

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
import com.example.diplomovka_kotlin.viewmodel.RegisterViewModel
import com.example.diplomovka_kotlin.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    private val vm: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        binding.btnRegister.setOnClickListener { if (validate()) register() }
    }

    private fun validate(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()

        if (email.isEmpty() || !email.contains('@')) {
            binding.tilEmail.error = "Zadajte platný e-mail."; return false
        } else binding.tilEmail.error = null

        if (password.length < 6) {
            binding.tilPassword.error = "Heslo musí mať aspoň 6 znakov."; return false
        } else binding.tilPassword.error = null

        if (confirm.isEmpty()) {
            binding.tilConfirmPassword.error = "Potvrdenie hesla je povinné."; return false
        } else binding.tilConfirmPassword.error = null

        if (password != confirm) {
            Toast.makeText(requireContext(), "❌ Heslá sa nezhodujú!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun register() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        lifecycleScope.launch {
            if (vm.register(email, password)) {
                Toast.makeText(
                    requireContext(),
                    "✅ Registrácia úspešná! Skontrolujte e-mail pre overenie.",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.action_register_to_login)
            }
        }
    }

    private fun observeViewModel() {
        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !loading
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
