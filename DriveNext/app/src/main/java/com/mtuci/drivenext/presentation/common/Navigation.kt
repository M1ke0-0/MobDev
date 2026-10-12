package com.mtuci.drivenext.presentation.common

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.mtuci.drivenext.DriveNextApp
import com.mtuci.drivenext.di.AppContainer
import com.mtuci.drivenext.domain.model.StartDestination
import com.mtuci.drivenext.presentation.auth.AuthChoiceActivity
import com.mtuci.drivenext.presentation.main.MainActivity
import com.mtuci.drivenext.presentation.noconnection.NoConnectionActivity
import com.mtuci.drivenext.presentation.onboarding.OnboardingActivity

fun Activity.openScreenAndFinish(target: Class<out Activity>) {
    startActivity(Intent(this, target))
    finish()
}

/** Открывает экран и очищает стек: «Назад» с него уже не вернёт на предыдущие экраны. */
fun Activity.openAsNewRoot(target: Class<out Activity>) {
    startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}

fun Activity.navigateTo(destination: StartDestination) {
    val target = when (destination) {
        StartDestination.ONBOARDING -> OnboardingActivity::class.java
        StartDestination.AUTH -> AuthChoiceActivity::class.java
        StartDestination.MAIN -> MainActivity::class.java
    }
    openScreenAndFinish(target)
}

/** Экран «Нет подключения» поверх текущего: когда сеть появится, он закроется и вернёт пользователя обратно. */
fun Activity.openNoConnection() {
    startActivity(NoConnectionActivity.returnIntent(this))
}

fun CreationExtras.appContainer(): AppContainer =
    (this[APPLICATION_KEY] as DriveNextApp).container
