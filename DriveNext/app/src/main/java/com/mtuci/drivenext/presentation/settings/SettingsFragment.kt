package com.mtuci.drivenext.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.FragmentSettingsBinding
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.loadAvatar
import com.mtuci.drivenext.presentation.common.openNoConnection
import com.mtuci.drivenext.presentation.common.showErrorDialog
import com.mtuci.drivenext.presentation.main.MainNavigator

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }
    private val navigator get() = requireActivity() as MainNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.profileRow.setOnClickListener { viewModel.onProfileClick() }
        mapOf(
            binding.rowBookings to SettingsItem.BOOKINGS,
            binding.rowTheme to SettingsItem.THEME,
            binding.rowNotifications to SettingsItem.NOTIFICATIONS,
            binding.rowAddCar to SettingsItem.ADD_CAR,
            binding.rowHelp to SettingsItem.HELP,
            binding.rowInvite to SettingsItem.INVITE
        ).forEach { (row, item) -> row.setOnClickListener { viewModel.onItemClick(item) } }

        viewLifecycleOwner.collectWhenStarted(viewModel.state, ::render)
        viewLifecycleOwner.collectWhenStarted(viewModel.events, ::handleEvent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenStarted()
    }

    private fun render(state: SettingsUiState) {
        val profile = state.profile
        // Пока профиля нет (первая загрузка), вместо имени и почты — индикатор
        binding.profileProgress.isVisible = profile == null && state.isLoading
        binding.tvName.isInvisible = profile == null
        binding.tvEmail.isInvisible = profile == null
        binding.tvName.text = profile?.fullName
        binding.tvEmail.text = profile?.email
        if (binding.ivAvatar.tag != profile?.avatarUrl) {
            binding.ivAvatar.tag = profile?.avatarUrl
            binding.ivAvatar.loadAvatar(profile?.avatarUrl)
        }
    }

    private fun handleEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OpenProfile -> navigator.openProfile()
            SettingsEvent.OpenMyBookings -> navigator.openMyBookings()
            is SettingsEvent.OpenComingSoon -> navigator.openComingSoon(event.titleRes)
            SettingsEvent.ShareInvite -> shareInvite()
            SettingsEvent.OpenNoConnection -> requireActivity().openNoConnection()
            SettingsEvent.OpenLogin -> navigator.openLogin()
            is SettingsEvent.ShowMessage ->
                Snackbar.make(binding.root, event.message.resolve(requireContext()), Snackbar.LENGTH_SHORT).show()
            is SettingsEvent.ShowError -> requireContext().showErrorDialog(event.message)
        }
    }

    /** «Пригласи друга» — системное окно «Поделиться» с текстом приглашения. */
    private fun shareInvite() {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_invite_text))
        startActivity(Intent.createChooser(send, getString(R.string.settings_invite)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
