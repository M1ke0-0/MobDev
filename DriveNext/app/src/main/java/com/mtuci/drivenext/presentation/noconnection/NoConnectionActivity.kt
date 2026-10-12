package com.mtuci.drivenext.presentation.noconnection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ActivityNoConnectionBinding
import com.mtuci.drivenext.presentation.common.navigateTo
import kotlinx.coroutines.launch

class NoConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoConnectionBinding
    private val viewModel: NoConnectionViewModel by viewModels { NoConnectionViewModel.Factory }
    private val returnWhenOnline: Boolean
        get() = intent.getBooleanExtra(EXTRA_RETURN_WHEN_ONLINE, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRetry.setOnClickListener { viewModel.onRetryClick() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isChecking.collect { checking ->
                        binding.progress.isVisible = checking
                        binding.btnRetry.isEnabled = !checking
                        binding.btnRetry.text = if (checking) "" else getString(R.string.no_connection_retry)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            NoConnectionEvent.StillOffline -> Snackbar
                                .make(binding.root, R.string.no_connection_still_offline, Snackbar.LENGTH_SHORT)
                                .show()
                            // Открыли поверх формы (вход, регистрация) — просто возвращаемся к ней.
                            is NoConnectionEvent.Navigate ->
                                if (returnWhenOnline) finish() else navigateTo(event.destination)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_RETURN_WHEN_ONLINE = "return_when_online"

        fun returnIntent(context: Context): Intent =
            Intent(context, NoConnectionActivity::class.java).putExtra(EXTRA_RETURN_WHEN_ONLINE, true)
    }
}
