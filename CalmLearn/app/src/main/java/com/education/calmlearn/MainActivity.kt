package com.education.calmlearn

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.education.calmlearn.data.auth.AuthRepositoryProvider
import com.education.calmlearn.data.onboarding.OnboardingPrefs
import com.education.calmlearn.databinding.ActivityMainBinding
import com.education.calmlearn.ui.start.StartDestination
import com.education.calmlearn.ui.start.StartDestinationResolver

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.lessonsFragment,
        R.id.practiceFragment,
        R.id.progressFragment,
        R.id.profileFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHost.navController

        // Chi doi diem bat dau luc Activity duoc tao lan dau (khong phai luc xoay man hinh):
        // gan lai navController.graph se lam mat back stack hien tai, nen KHONG duoc goi khi
        // savedInstanceState != null - luc do NavHostFragment tu khoi phuc dung man hinh dang mo.
        if (savedInstanceState == null) {
            applyStartDestination(navController)
        }

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility =
                if (destination.id in topLevelDestinations) View.VISIBLE else View.GONE
        }
    }

    private fun applyStartDestination(navController: NavController) {
        // isAuthenticated() la ham dong bo (khong suspend) theo dung interface AuthRepository hien
        // tai nen chua can man hinh cho/splash rieng. Neu sau nay noi dich vu that ma viec kiem tra
        // phien can thoi gian (vd goi mang), hay doi diem nay sang mot trang thai cho ro rang
        // truoc khi quyet dinh, tranh nhay man hinh sai.
        val onboardingCompleted = OnboardingPrefs(this).isCompleted()
        val isAuthenticated = AuthRepositoryProvider.repository.isAuthenticated()
        val destination = StartDestinationResolver.resolve(onboardingCompleted, isAuthenticated)

        val destinationId = when (destination) {
            StartDestination.ONBOARDING -> R.id.onboardingFragment
            StartDestination.LOGIN -> R.id.loginFragment
            StartDestination.HOME -> R.id.homeFragment
        }

        // onboardingFragment da la start destination mac dinh khai bao trong nav_graph.xml nen
        // khong can inflate lai graph cho truong hop nay.
        if (destinationId != R.id.onboardingFragment) {
            navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
                setStartDestination(destinationId)
            }
        }
    }
}
