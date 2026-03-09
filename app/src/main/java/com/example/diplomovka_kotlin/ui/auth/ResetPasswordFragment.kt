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
import com.example.diplomovka_kotlin.viewmodel.ResetPasswordViewModel
import com.example.diplomovka_kotlin.databinding.ActivityResetPasswordBinding
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment() {

    private var _binding: ActivityResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val vm: ResetPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        binding.btnSendEmail.setOnClickListener { if (validate()) sendResetEmail() }
    }

    private fun validate(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        return if (email.isEmpty() || !email.contains('@')) {
            binding.tilEmail.error = "Zadajte platný e-mail."
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }

    private fun sendResetEmail() {
        lifecycleScope.launch {
            if (vm.sendResetEmail(binding.etEmail.text.toString().trim())) {
                Toast.makeText(
                    requireContext(),
                    "✅ E-mail na reset hesla bol odoslaný. Skontrolujte schránku!",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun observeViewModel() {
        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSendEmail.isEnabled = !loading
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
