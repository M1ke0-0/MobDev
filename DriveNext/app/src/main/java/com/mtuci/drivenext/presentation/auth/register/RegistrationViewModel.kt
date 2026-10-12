package com.mtuci.drivenext.presentation.auth.register

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mtuci.drivenext.R
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.model.Gender
import com.mtuci.drivenext.domain.model.RegistrationData
import com.mtuci.drivenext.domain.usecase.RegisterUseCase
import com.mtuci.drivenext.domain.validation.Validators
import com.mtuci.drivenext.presentation.common.UiText
import com.mtuci.drivenext.presentation.common.appContainer
import com.mtuci.drivenext.presentation.common.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class PhotoTarget { AVATAR, LICENSE, PASSPORT }

/** Состояние всех трёх шагов. Одна ViewModel на Activity — поэтому при «Назад» поля не теряются. */
data class RegistrationUiState(
    // Шаг 1
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    @StringRes val termsError: Int? = null,
    // Шаг 2
    val lastName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val birthDate: String = "",
    val gender: Gender? = null,
    @StringRes val birthDateError: Int? = null,
    // Шаг 3
    val licenseNumber: String = "",
    val issueDate: String = "",
    val avatarUri: String? = null,
    val licensePhotoUri: String? = null,
    val passportPhotoUri: String? = null,
    @StringRes val licenseNumberError: Int? = null,
    @StringRes val issueDateError: Int? = null,
    val isLoading: Boolean = false
) {
    val isStep2NextEnabled: Boolean
        get() = lastName.isNotEmpty() && firstName.isNotEmpty() && birthDate.isNotEmpty() && gender != null

    val isStep3NextEnabled: Boolean
        get() = licenseNumber.isNotEmpty() && issueDate.isNotEmpty() && !isLoading
}

sealed interface RegistrationEvent {
    data object OpenStep2 : RegistrationEvent
    data object OpenStep3 : RegistrationEvent
    data object Registered : RegistrationEvent
    data object OpenNoConnection : RegistrationEvent
    data class ShowMessage(val message: UiText) : RegistrationEvent
    data class ShowError(val message: UiText) : RegistrationEvent
}

class RegistrationViewModel(
    private val register: RegisterUseCase,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationUiState())
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()

    private val _events = Channel<RegistrationEvent>(Channel.BUFFERED)
    val events: Flow<RegistrationEvent> = _events.receiveAsFlow()

    // Шаг 1

    fun onEmailChanged(value: String) = _state.update { it.copy(email = value, emailError = null) }
    fun onPasswordChanged(value: String) = _state.update { it.copy(password = value, passwordError = null) }
    fun onConfirmPasswordChanged(value: String) =
        _state.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    fun onTermsChanged(checked: Boolean) = _state.update { it.copy(termsAccepted = checked, termsError = null) }

    fun onStep1Next() {
        val s = _state.value
        val emailError = R.string.error_invalid_email.takeUnless { Validators.isValidEmail(s.email) }
        val passwordError = when {
            s.password.isEmpty() -> R.string.error_required_fields
            s.password.length < MIN_PASSWORD_LENGTH -> R.string.error_password_short
            else -> null
        }
        val confirmError = when {
            s.confirmPassword.isEmpty() -> R.string.error_required_fields
            s.confirmPassword != s.password -> R.string.error_passwords_mismatch
            else -> null
        }
        val termsError = R.string.error_terms_not_accepted.takeUnless { s.termsAccepted }

        _state.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmError,
                termsError = termsError
            )
        }
        if (listOf(emailError, passwordError, confirmError, termsError).all { it == null }) {
            send(RegistrationEvent.OpenStep2)
        }
    }

    // Шаг 2

    fun onLastNameChanged(value: String) = _state.update { it.copy(lastName = value) }
    fun onFirstNameChanged(value: String) = _state.update { it.copy(firstName = value) }
    fun onMiddleNameChanged(value: String) = _state.update { it.copy(middleName = value) }
    fun onBirthDateChanged(value: String) = _state.update { it.copy(birthDate = value, birthDateError = null) }
    fun onBirthDatePicked(date: LocalDate) = onBirthDateChanged(Validators.formatDate(date))
    fun onGenderChanged(gender: Gender) = _state.update { it.copy(gender = gender) }

    fun onStep2Next() {
        val s = _state.value
        if (s.lastName.isBlank() || s.firstName.isBlank() || s.birthDate.isBlank() || s.gender == null) {
            send(RegistrationEvent.ShowMessage(UiText(R.string.error_required_fields)))
            return
        }
        if (Validators.parseBirthDate(s.birthDate) == null) {
            _state.update { it.copy(birthDateError = R.string.error_invalid_birth_date) }
            return
        }
        send(RegistrationEvent.OpenStep3)
    }

    // Шаг 3

    fun onLicenseNumberChanged(value: String) =
        _state.update { it.copy(licenseNumber = value, licenseNumberError = null) }
    fun onIssueDateChanged(value: String) = _state.update { it.copy(issueDate = value, issueDateError = null) }
    fun onIssueDatePicked(date: LocalDate) = onIssueDateChanged(Validators.formatDate(date))

    /** Запоминаем, для какого фото открыли галерею/камеру — результат придёт позже. */
    fun onPhotoSourceRequested(target: PhotoTarget) {
        savedState[KEY_PENDING_TARGET] = target.name
    }

    /** Камере нужен заранее созданный Uri — сохраняем его, чтобы пережить даже пересоздание процесса. */
    fun onCameraUriCreated(uri: String) {
        savedState[KEY_PENDING_CAMERA_URI] = uri
    }

    fun onGalleryResult(uri: String?) {
        if (uri != null) setPhoto(uri)
    }

    fun onCameraResult(success: Boolean) {
        val uri = savedState.get<String>(KEY_PENDING_CAMERA_URI)
        if (success && uri != null) setPhoto(uri)
        savedState.remove<String>(KEY_PENDING_CAMERA_URI)
    }

    private fun setPhoto(uri: String) {
        val target = savedState.get<String>(KEY_PENDING_TARGET)?.let(PhotoTarget::valueOf) ?: return
        _state.update {
            when (target) {
                PhotoTarget.AVATAR -> it.copy(avatarUri = uri)
                PhotoTarget.LICENSE -> it.copy(licensePhotoUri = uri)
                PhotoTarget.PASSPORT -> it.copy(passportPhotoUri = uri)
            }
        }
    }

    fun onStep3Next() {
        val s = _state.value
        if (!s.isStep3NextEnabled) return
        if (s.licenseNumber.isBlank() || s.issueDate.isBlank()) {
            send(RegistrationEvent.ShowMessage(UiText(R.string.error_required_fields)))
            return
        }
        val birthDate = Validators.parseBirthDate(s.birthDate)
        val issueDate = Validators.parseIssueDate(s.issueDate, birthDate)
        val licenseError = R.string.error_license_number.takeUnless { Validators.isValidLicenseNumber(s.licenseNumber) }
        val issueDateError = R.string.error_invalid_issue_date.takeIf { issueDate == null }
        _state.update { it.copy(licenseNumberError = licenseError, issueDateError = issueDateError) }
        if (licenseError != null || issueDate == null || birthDate == null) return

        val licensePhoto = s.licensePhotoUri
        val passportPhoto = s.passportPhotoUri
        if (licensePhoto == null || passportPhoto == null) {
            send(RegistrationEvent.ShowMessage(UiText(R.string.error_photos_required)))
            return
        }

        val data = RegistrationData(
            email = s.email,
            password = s.password,
            lastName = s.lastName.trim(),
            firstName = s.firstName.trim(),
            middleName = s.middleName.trim().ifEmpty { null },
            birthDate = birthDate,
            gender = s.gender ?: return,
            licenseNumber = s.licenseNumber,
            licenseIssueDate = issueDate,
            avatarUri = s.avatarUri,
            licensePhotoUri = licensePhoto,
            passportPhotoUri = passportPhoto
        )
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = register(data)
            _state.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { _events.send(RegistrationEvent.Registered) },
                onFailure = { error ->
                    _events.send(
                        if (error is AppError.NoConnection) RegistrationEvent.OpenNoConnection
                        else RegistrationEvent.ShowError(error.toUiText())
                    )
                }
            )
        }
    }

    private fun send(event: RegistrationEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val KEY_PENDING_TARGET = "pending_photo_target"
        private const val KEY_PENDING_CAMERA_URI = "pending_camera_uri"

        val Factory = viewModelFactory {
            initializer {
                RegistrationViewModel(appContainer().registerUseCase, createSavedStateHandle())
            }
        }
    }
}
