package com.mtuci.drivenext.presentation.auth.register

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.FragmentRegisterStep3Binding
import com.mtuci.drivenext.presentation.common.DateMaskWatcher
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.showError
import com.mtuci.drivenext.presentation.common.setTextIfChanged
import com.mtuci.drivenext.presentation.common.reattachDatePicker
import com.mtuci.drivenext.presentation.common.showDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID

class RegisterStep3Fragment : Fragment() {

    private var _binding: FragmentRegisterStep3Binding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistrationViewModel by activityViewModels { RegistrationViewModel.Factory }

    // Activity Result API: системный выбор фото. Разрешение на чтение галереи не нужно — доступ даётся через Uri.
    private val pickOneImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        viewModel.onGalleryResult(uri?.toString())
    }

    // Камера сохраняет снимок в заранее подготовленный Uri (FileProvider) и возвращает true при успехе
    private val takeOnePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        viewModel.onCameraResult(success)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val state = viewModel.state.value
        binding.etLicenseNumber.setTextIfChanged(state.licenseNumber)
        binding.etIssueDate.setTextIfChanged(state.issueDate)

        binding.avatarContainer.setOnClickListener { choosePhotoSource(PhotoTarget.AVATAR) }
        binding.licensePhotoRow.setOnClickListener { choosePhotoSource(PhotoTarget.LICENSE) }
        binding.passportPhotoRow.setOnClickListener { choosePhotoSource(PhotoTarget.PASSPORT) }

        binding.etLicenseNumber.doAfterTextChanged { viewModel.onLicenseNumberChanged(it.toString()) }
        binding.etIssueDate.addTextChangedListener(DateMaskWatcher(binding.etIssueDate))
        binding.etIssueDate.doAfterTextChanged { viewModel.onIssueDateChanged(binding.etIssueDate.text.toString()) }
        binding.tilIssueDate.setStartIconOnClickListener {
            childFragmentManager.showDatePicker(
                DATE_PICKER_TAG, getString(R.string.pick_issue_date), binding.etIssueDate.text.toString(), ::onDatePicked
            )
        }
        childFragmentManager.reattachDatePicker(DATE_PICKER_TAG, ::onDatePicked)

        binding.btnNext.setOnClickListener { viewModel.onStep3Next() }

        viewLifecycleOwner.collectWhenStarted(viewModel.state, ::render)
    }

    private fun render(state: RegistrationUiState) {
        binding.tilLicenseNumber.showError(state.licenseNumberError?.let(::getString))
        binding.tilIssueDate.showError(state.issueDateError?.let(::getString))
        binding.btnNext.isEnabled = state.isStep3NextEnabled

        binding.ivAvatar.showPhoto(state.avatarUri, R.drawable.ic_avatar_placeholder)
        binding.ivLicensePhoto.showPhoto(state.licensePhotoUri, R.drawable.ic_upload)
        binding.ivPassportPhoto.showPhoto(state.passportPhotoUri, R.drawable.ic_upload)
        binding.tvLicensePhoto.setText(if (state.licensePhotoUri != null) R.string.photo_uploaded else R.string.upload_photo)
        binding.tvPassportPhoto.setText(if (state.passportPhotoUri != null) R.string.photo_uploaded else R.string.upload_photo)
    }

    private fun onDatePicked(date: LocalDate) {
        viewModel.onIssueDatePicked(date)
        binding.etIssueDate.setText(viewModel.state.value.issueDate)
    }

    private fun choosePhotoSource(target: PhotoTarget) {
        viewModel.onPhotoSourceRequested(target)
        val sources = arrayOf(getString(R.string.photo_source_gallery), getString(R.string.photo_source_camera))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.photo_source_title)
            .setItems(sources) { _, which -> if (which == 0) openGallery() else openCamera() }
            .show()
    }

    private fun openGallery() {
        pickOneImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        val context = requireContext()
        val file = File(context.cacheDir, "photos/${UUID.randomUUID()}.jpg").apply { parentFile?.mkdirs() }
        // Без FileProvider камера (другое приложение) не сможет записать файл в нашу папку
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        viewModel.onCameraUriCreated(uri.toString())
        try {
            takeOnePhoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, R.string.camera_unavailable, Snackbar.LENGTH_SHORT).show()
        }
    }

    /** Показывает уменьшенную копию фото, чтобы не грузить в память снимок с камеры целиком. */
    private fun ImageView.showPhoto(uri: String?, @DrawableRes placeholder: Int) {
        if (tag == uri && drawable != null) return
        tag = uri
        if (uri == null) {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(placeholder)
            return
        }
        val resolver = context.contentResolver
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching { resolver.loadThumbnail(Uri.parse(uri), Size(THUMBNAIL_SIZE, THUMBNAIL_SIZE), null) }.getOrNull()
            }
            if (tag != uri) return@launch
            if (bitmap != null) {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(bitmap)
            } else {
                setImageResource(placeholder)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val DATE_PICKER_TAG = "issue_date_picker"
        const val THUMBNAIL_SIZE = 384
    }
}
