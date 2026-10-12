package com.mtuci.drivenext.presentation.splash

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mtuci.drivenext.BuildConfig
import com.mtuci.drivenext.DriveNextApp
import com.mtuci.drivenext.databinding.ActivitySplashBinding
import com.mtuci.drivenext.presentation.common.navigateTo
import com.mtuci.drivenext.presentation.common.openScreenAndFinish
import com.mtuci.drivenext.presentation.noconnection.NoConnectionActivity
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels { SplashViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Должен вызываться ДО super.onCreate, иначе тема сплэша не переключится на основную.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(ActivitySplashBinding.inflate(layoutInflater).root)

        // Сброс до создания ViewModel, чтобы стартовый экран считался уже по чистым данным.
        if (BuildConfig.DEBUG && savedInstanceState == null && intent.action == ACTION_RESET) {
            (application as DriveNextApp).container.resetAppData()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        SplashEvent.NoConnection -> openScreenAndFinish(NoConnectionActivity::class.java)
                        is SplashEvent.Navigate -> navigateTo(event.destination)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_RESET = "com.mtuci.drivenext.action.RESET"
    }
}
