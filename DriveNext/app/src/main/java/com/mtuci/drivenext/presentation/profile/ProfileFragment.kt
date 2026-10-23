package com.mtuci.drivenext.presentation.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.FragmentProfileBinding
import com.mtuci.drivenext.domain.model.Gender
import com.mtuci.drivenext.domain.model.UserProfile
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.loadAvatar
import com.mtuci.drivenext.presentation.common.openNoConnection
import com.mtuci.drivenext.presentation.common.showErrorDialog
import com.mtuci.drivenext.presentation.main.MainNavigator

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory }

    // Системный выбор фото, как на шаге 3 регистрации: разрешения на галерею не нужны
    private val pickAvatar = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        viewModel.onAvatarPicked(uri?.toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.avatarContainer.setOnClickListener {
            pickAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.tvChangePassword.setOnClickListener { viewModel.onChangePasswordClick() }
        binding.tvSignOut.setOnClickListener { viewModel.onSignOutClick() }

        viewLifecycleOwner.collectWhenStarted(viewModel.state, ::render)
        viewLifecycleOwner.collectWhenStarted(viewModel.events, ::handleEvent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenStarted()
    }

    private fun render(state: ProfileUiState) {
        binding.loadingOverlay.isVisible = state.isLoading || state.isSaving
        binding.tvLoading.isVisible = state.isSaving
        state.profile?.let(::showProfile)
    }

    private fun showProfile(profile: UserProfile) {
        binding.tvName.text = profile.fullName
        binding.tvJoined.isVisible = profile.joinedAt != null
        profile.joinedAt?.let { date ->
            val month = resources.getStringArray(R.array.months_prepositional)[date.monthValue - 1]
            val text = if (profile.gender == Gender.FEMALE) R.string.profile_joined_female else R.string.profile_joined_male
            binding.tvJoined.text = getString(text, month, date.year)
        }
        binding.tvEmail.text = profile.email
        binding.tvGender.setText(
            when (profile.gender) {
                Gender.MALE -> R.string.gender_male
                Gender.FEMALE -> R.string.gender_female
                null -> R.string.profile_gender_unknown
            }
        )
        binding.tvGoogle.text = profile.googleEmail ?: getString(R.string.profile_google_not_linked)
        if (binding.ivAvatar.tag != profile.avatarUrl) {
            binding.ivAvatar.tag = profile.avatarUrl
            binding.ivAvatar.loadAvatar(profile.avatarUrl)
        }
    }

    private fun handleEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OpenLogin -> (requireActivity() as MainNavigator).openLogin()
            ProfileEvent.OpenNoConnection -> requireActivity().openNoConnection()
            is ProfileEvent.ShowMessage ->
                Snackbar.make(binding.root, event.message.resolve(requireContext()), Snackbar.LENGTH_LONG).show()
            is ProfileEvent.ShowError -> requireContext().showErrorDialog(event.message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
