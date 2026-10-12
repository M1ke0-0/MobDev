package com.mtuci.drivenext.presentation.auth.register

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.snackbar.Snackbar
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ActivityRegistrationBinding
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.openAsNewRoot
import com.mtuci.drivenext.presentation.common.openNoConnection
import com.mtuci.drivenext.presentation.common.showErrorDialog

/**
 * Контейнер для трёх шагов регистрации. Шаги — фрагменты в back stack,
 * поэтому «Назад» возвращает на предыдущий шаг, а с первого — на предыдущий экран.
 */
class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel: RegistrationViewModel by viewModels { RegistrationViewModel.Factory }

    // Пока идёт отправка данных, «Назад» не прерывает регистрацию
    private val blockBackWhileLoading = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback(this, blockBackWhileLoading)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.container, RegisterStep1Fragment())
            }
        }

        collectWhenStarted(viewModel.state) { state ->
            binding.loadingOverlay.isVisible = state.isLoading
            blockBackWhileLoading.isEnabled = state.isLoading
        }
        collectWhenStarted(viewModel.events, ::handleEvent)
    }

    private fun handleEvent(event: RegistrationEvent) {
        when (event) {
            RegistrationEvent.OpenStep2 -> openStep(RegisterStep2Fragment())
            RegistrationEvent.OpenStep3 -> openStep(RegisterStep3Fragment())
            RegistrationEvent.Registered -> openAsNewRoot(RegistrationSuccessActivity::class.java)
            RegistrationEvent.OpenNoConnection -> openNoConnection()
            is RegistrationEvent.ShowMessage ->
                Snackbar.make(binding.root, event.message.resolve(this), Snackbar.LENGTH_LONG).show()
            is RegistrationEvent.ShowError -> showErrorDialog(event.message)
        }
    }

    private fun openStep(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.slide_out_right
            )
            replace(R.id.container, fragment)
            addToBackStack(null)
        }
    }
}
