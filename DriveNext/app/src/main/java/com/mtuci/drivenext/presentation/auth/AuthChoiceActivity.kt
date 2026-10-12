package com.mtuci.drivenext.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mtuci.drivenext.databinding.ActivityAuthChoiceBinding
import com.mtuci.drivenext.presentation.auth.login.LoginActivity
import com.mtuci.drivenext.presentation.auth.register.RegistrationActivity

/** Экран «Вход/регистрация». Логики нет — только переходы, поэтому ViewModel не нужна. */
class AuthChoiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        binding.btnSignUp.setOnClickListener { startActivity(Intent(this, RegistrationActivity::class.java)) }
    }
}
