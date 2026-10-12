package com.mtuci.drivenext.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.FragmentRegisterStep2Binding
import com.mtuci.drivenext.domain.model.Gender
import com.mtuci.drivenext.domain.validation.Validators
import com.mtuci.drivenext.presentation.common.DateMaskWatcher
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.showError
import com.mtuci.drivenext.presentation.common.setTextIfChanged
import com.mtuci.drivenext.presentation.common.reattachDatePicker
import com.mtuci.drivenext.presentation.common.showDatePicker
import java.time.LocalDate

class RegisterStep2Fragment : Fragment() {

    private var _binding: FragmentRegisterStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistrationViewModel by activityViewModels { RegistrationViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val state = viewModel.state.value
        binding.etLastName.setTextIfChanged(state.lastName)
        binding.etFirstName.setTextIfChanged(state.firstName)
        binding.etMiddleName.setTextIfChanged(state.middleName)
        binding.etBirthDate.setTextIfChanged(state.birthDate)
        when (state.gender) {
            Gender.MALE -> binding.rgGender.check(R.id.rbMale)
            Gender.FEMALE -> binding.rgGender.check(R.id.rbFemale)
            null -> binding.rgGender.clearCheck()
        }

        binding.etLastName.doAfterTextChanged { viewModel.onLastNameChanged(it.toString()) }
        binding.etFirstName.doAfterTextChanged { viewModel.onFirstNameChanged(it.toString()) }
        binding.etMiddleName.doAfterTextChanged { viewModel.onMiddleNameChanged(it.toString()) }

        // Дату можно ввести руками (маска DD/MM/YYYY) или выбрать в календаре по иконке
        binding.etBirthDate.addTextChangedListener(DateMaskWatcher(binding.etBirthDate))
        binding.etBirthDate.doAfterTextChanged { viewModel.onBirthDateChanged(binding.etBirthDate.text.toString()) }
        binding.tilBirthDate.setStartIconOnClickListener {
            childFragmentManager.showDatePicker(
                DATE_PICKER_TAG, getString(R.string.pick_birth_date), binding.etBirthDate.text.toString(), ::onDatePicked,
                openAt = LocalDate.now().minusYears(Validators.MIN_DRIVER_AGE.toLong())
            )
        }
        childFragmentManager.reattachDatePicker(DATE_PICKER_TAG, ::onDatePicked)

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMale -> viewModel.onGenderChanged(Gender.MALE)
                R.id.rbFemale -> viewModel.onGenderChanged(Gender.FEMALE)
            }
        }
        binding.btnNext.setOnClickListener { viewModel.onStep2Next() }

        viewLifecycleOwner.collectWhenStarted(viewModel.state) { state ->
            binding.tilBirthDate.showError(state.birthDateError?.let(::getString))
            binding.btnNext.isEnabled = state.isStep2NextEnabled
        }
    }

    private fun onDatePicked(date: LocalDate) {
        viewModel.onBirthDatePicked(date)
        binding.etBirthDate.setText(viewModel.state.value.birthDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val DATE_PICKER_TAG = "birth_date_picker"
    }
}
