package com.mtuci.drivenext.presentation.auth.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ActivityLoginBinding
import com.mtuci.drivenext.presentation.auth.register.RegistrationActivity
import com.mtuci.drivenext.presentation.common.collectWhenStarted
import com.mtuci.drivenext.presentation.common.showError
import com.mtuci.drivenext.presentation.common.openAsNewRoot
import com.mtuci.drivenext.presentation.common.openNoConnection
import com.mtuci.drivenext.presentation.common.showErrorDialog
import com.mtuci.drivenext.presentation.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels { LoginViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.doAfterTextChanged { viewModel.onEmailChanged(it.toString()) }
        binding.etPassword.doAfterTextChanged { viewModel.onPasswordChanged(it.toString()) }
        binding.btnSignIn.setOnClickListener { viewModel.onSignInClick() }
        binding.btnGoogle.setOnClickListener { viewModel.onGoogleClick() }
        binding.tvForgotPassword.setOnClickListener { viewModel.onForgotPasswordClick() }
        binding.tvRegister.setOnClickListener { viewModel.onRegisterClick() }

        collectWhenStarted(viewModel.state, ::render)
        collectWhenStarted(viewModel.events, ::handleEvent)

        // savedInstanceState == null: после поворота ту же ссылку второй раз не обрабатываем
        if (savedInstanceState == null) handleAuthCallback(intent)
    }

    // launchMode="singleTask": возврат из браузера после входа через Google приходит сюда
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthCallback(intent)
    }

    private fun handleAuthCallback(intent: Intent) {
        intent.data?.let { viewModel.onAuthCallback(it.toString()) }
    }

    private fun render(state: LoginUiState) {
        binding.tilEmail.showError(state.emailError?.let(::getString))
        binding.btnSignIn.isEnabled = state.isSignInEnabled
        binding.btnSignIn.isInvisible = state.isLoading
        binding.progress.isVisible = state.isLoading
        binding.btnGoogle.isEnabled = !state.isLoading
    }

    private fun handleEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.OpenMain -> openAsNewRoot(MainActivity::class.java)
            LoginEvent.OpenRegistration -> startActivity(Intent(this, RegistrationActivity::class.java))
            LoginEvent.OpenNoConnection -> openNoConnection()
            is LoginEvent.ShowError -> showErrorDialog(event.message)
            is LoginEvent.ShowMessage ->
                Snackbar.make(binding.root, event.message.resolve(this), Snackbar.LENGTH_LONG).show()
        }
    }
}
