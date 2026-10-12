package com.mtuci.drivenext.presentation.auth.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mtuci.drivenext.databinding.ActivityRegistrationSuccessBinding
import com.mtuci.drivenext.presentation.common.openAsNewRoot
import com.mtuci.drivenext.presentation.main.MainActivity

class RegistrationSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegistrationSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // По ТЗ: переход на главный экран с очисткой стека навигации
        binding.btnNext.setOnClickListener { openAsNewRoot(MainActivity::class.java) }
    }
}
