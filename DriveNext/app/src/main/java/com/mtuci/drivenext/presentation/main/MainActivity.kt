package com.mtuci.drivenext.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.mtuci.drivenext.R
import com.mtuci.drivenext.databinding.ActivityMainBinding
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.presentation.auth.login.LoginActivity
import com.mtuci.drivenext.presentation.booking.BookingFragment
import com.mtuci.drivenext.presentation.booking.BookingSuccessActivity
import com.mtuci.drivenext.presentation.bookings.BookingDetailsFragment
import com.mtuci.drivenext.presentation.bookings.MyBookingsFragment
import com.mtuci.drivenext.presentation.common.openAsNewRoot
import com.mtuci.drivenext.presentation.details.CarDetailsFragment
import com.mtuci.drivenext.presentation.favorites.FavoritesFragment
import com.mtuci.drivenext.presentation.home.HomeFragment
import com.mtuci.drivenext.presentation.profile.ProfileFragment
import com.mtuci.drivenext.presentation.search.SearchResultsFragment
import com.mtuci.drivenext.presentation.settings.SettingsFragment

/**
 * Главный экран с нижним меню. Каждая вкладка — фрагмент в контейнере над меню.
 * Вложенные экраны («Результаты поиска», «Детали», «Оформление аренды»…) кладутся в back stack поверх вкладки,
 * поэтому меню остаётся видно, а «Назад» возвращает на корень вкладки.
 */
class MainActivity : AppCompatActivity(), MainNavigator {

    private lateinit var binding: ActivityMainBinding

    // «Назад» на корне вкладки «Избранное» или «Настройки» сначала возвращает на «Главную»
    private val backToHome = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
        // Повторное нажатие на текущую вкладку — вернуться к её началу
        binding.bottomNav.setOnItemReselectedListener { popToTabRoot() }

        onBackPressedDispatcher.addCallback(this, backToHome)
        supportFragmentManager.addOnBackStackChangedListener { updateBackToHome() }

        // После поворота фрагменты и выбранная вкладка восстанавливаются сами
        if (savedInstanceState == null) {
            showTab(R.id.nav_home)
            // Пришли с «Удачной поездки» по кнопке «Перейти к своим бронированиям»
            if (intent.getBooleanExtra(EXTRA_OPEN_BOOKINGS, false)) openMyBookings()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Выбранная вкладка восстанавливается здесь, а не в onCreate
        updateBackToHome()
    }

    private fun showTab(@IdRes itemId: Int) {
        popToTabRoot()
        val fragment = when (itemId) {
            R.id.nav_favorites -> FavoritesFragment()
            R.id.nav_settings -> SettingsFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.container, fragment)
        }
        // selectedItemId обновится только после выхода из слушателя, поэтому смотрим на itemId
        backToHome.isEnabled = itemId != R.id.nav_home
    }

    private fun popToTabRoot() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun updateBackToHome() {
        backToHome.isEnabled = supportFragmentManager.backStackEntryCount == 0 &&
            binding.bottomNav.selectedItemId != R.id.nav_home
    }

    private fun push(fragment: Fragment) {
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

    override fun openSearchResults(query: String) = push(SearchResultsFragment.newInstance(query))

    override fun openProfile() = push(ProfileFragment())

    override fun openComingSoon(@StringRes titleRes: Int) = push(ComingSoonFragment.newInstance(titleRes))

    override fun openCarDetails(carId: String) = push(CarDetailsFragment.newInstance(carId))

    override fun openBooking(car: Car) = push(BookingFragment.newInstance(car))

    override fun openMyBookings() = push(MyBookingsFragment())

    override fun openBookingDetails(bookingId: Long) = push(BookingDetailsFragment.newInstance(bookingId))

    override fun openBookingSuccess() {
        startActivity(BookingSuccessActivity.intent(this))
    }

    override fun openLogin() = openAsNewRoot(LoginActivity::class.java)

    companion object {
        private const val EXTRA_OPEN_BOOKINGS = "open_bookings"

        fun intent(context: Context, openBookings: Boolean = false) = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN_BOOKINGS, openBookings)
    }
}
