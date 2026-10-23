package com.mtuci.drivenext.presentation.booking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.mtuci.drivenext.databinding.ActivityBookingSuccessBinding
import com.mtuci.drivenext.presentation.main.MainActivity

/** «Удачной поездки». Стек уже очищен, поэтому любая кнопка открывает главный экран заново. */
class BookingSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBookingSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHome.setOnClickListener { openMain(openBookings = false) }
        binding.btnMyBookings.setOnClickListener { openMain(openBookings = true) }
        // Возвращаться на оформление аренды нельзя — «Назад» ведёт домой
        onBackPressedDispatcher.addCallback(this) { openMain(openBookings = false) }
    }

    private fun openMain(openBookings: Boolean) {
        startActivity(MainActivity.intent(this, openBookings))
        finish()
    }

    companion object {
        fun intent(context: Context) = Intent(context, BookingSuccessActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
