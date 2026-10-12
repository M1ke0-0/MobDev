package com.mtuci.drivenext.presentation.onboarding

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ActivityOnboardingBinding
import com.mtuci.drivenext.presentation.auth.AuthChoiceActivity
import com.mtuci.drivenext.presentation.common.openScreenAndFinish
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels { OnboardingViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = OnboardingPagerAdapter(this, viewModel.pages)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = viewModel.onPageSelected(position)
        })
        createDots(viewModel.pages.size)

        binding.btnNext.setOnClickListener { viewModel.onNextClick() }
        binding.btnSkip.setOnClickListener { viewModel.onSkipClick() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.currentPage.collect(::renderPage) }
                launch { viewModel.finished.collect { openScreenAndFinish(AuthChoiceActivity::class.java) } }
            }
        }
    }

    private fun renderPage(position: Int) {
        if (binding.viewPager.currentItem != position) {
            binding.viewPager.setCurrentItem(position, true)
        }
        binding.btnNext.setText(
            if (viewModel.isLastPage(position)) R.string.onboarding_go else R.string.onboarding_next
        )
        updateDots(position)
    }

    private fun createDots(count: Int) {
        repeat(count) {
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(DOT_WIDTH_DP), dp(DOT_HEIGHT_DP)).apply {
                    marginEnd = dp(DOT_SPACING_DP)
                }
            }
            binding.dotsContainer.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        binding.dotsContainer.children.forEachIndexed { index, dot ->
            val isActive = index == selected
            dot.layoutParams = dot.layoutParams.apply {
                width = dp(if (isActive) ACTIVE_DOT_WIDTH_DP else DOT_WIDTH_DP)
            }
            dot.setBackgroundResource(if (isActive) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val DOT_WIDTH_DP = 8
        const val ACTIVE_DOT_WIDTH_DP = 24
        const val DOT_HEIGHT_DP = 4
        const val DOT_SPACING_DP = 6
    }
}
