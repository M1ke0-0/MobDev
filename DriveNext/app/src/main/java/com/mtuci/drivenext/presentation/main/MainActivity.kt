package com.mtuci.drivenext.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ActivityPlaceholderBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPlaceholderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvTitle.setText(R.string.main_placeholder)
    }
}
