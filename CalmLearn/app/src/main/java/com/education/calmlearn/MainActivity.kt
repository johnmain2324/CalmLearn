package com.education.calmlearn

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.education.calmlearn.data.auth.AuthRepositoryProvider
import com.education.calmlearn.data.onboarding.OnboardingPrefs
import com.education.calmlearn.data.vocab.VocabRepositoryProvider
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
        VocabRepositoryProvider.init(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHost.navController

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
        val onboardingCompleted = OnboardingPrefs(this).isCompleted()
        val isAuthenticated = AuthRepositoryProvider.repository.isAuthenticated()
        val destination = StartDestinationResolver.resolve(onboardingCompleted, isAuthenticated)

        val destinationId = when (destination) {
            StartDestination.ONBOARDING -> R.id.onboardingFragment
            StartDestination.LOGIN -> R.id.loginFragment
            StartDestination.HOME -> R.id.homeFragment
        }

        if (destinationId != R.id.onboardingFragment) {
            navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
                setStartDestination(destinationId)
            }
        }
    }
}
