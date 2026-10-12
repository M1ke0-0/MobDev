package com.mtuci.drivenext.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mtuci.drivenext.databinding.FragmentRegisterStep1Binding
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.showError
import com.mtuci.drivenext.presentation.common.setTextIfChanged

class RegisterStep1Fragment : Fragment() {

    private var _binding: FragmentRegisterStep1Binding? = null
    private val binding get() = _binding!!

    // Общая с Activity и другими шагами ViewModel
    private val viewModel: RegistrationViewModel by activityViewModels { RegistrationViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val state = viewModel.state.value
        binding.etEmail.setTextIfChanged(state.email)
        binding.etPassword.setTextIfChanged(state.password)
        binding.etConfirmPassword.setTextIfChanged(state.confirmPassword)
        binding.cbTerms.isChecked = state.termsAccepted

        binding.etEmail.doAfterTextChanged { viewModel.onEmailChanged(it.toString()) }
        binding.etPassword.doAfterTextChanged { viewModel.onPasswordChanged(it.toString()) }
        binding.etConfirmPassword.doAfterTextChanged { viewModel.onConfirmPasswordChanged(it.toString()) }
        binding.cbTerms.setOnCheckedChangeListener { _, checked -> viewModel.onTermsChanged(checked) }
        binding.btnNext.setOnClickListener { viewModel.onStep1Next() }

        viewLifecycleOwner.collectWhenStarted(viewModel.state) { state ->
            binding.tilEmail.showError(state.emailError?.let(::getString))
            binding.tilPassword.showError(state.passwordError?.let(::getString))
            binding.tilConfirmPassword.showError(state.confirmPasswordError?.let(::getString))
            binding.tvTermsError.isVisible = state.termsError != null
            state.termsError?.let(binding.tvTermsError::setText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
