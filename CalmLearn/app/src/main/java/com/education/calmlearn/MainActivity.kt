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
import com.education.calmlearn.ui.chatbot.ChatBubbleGate
import com.education.calmlearn.ui.chatbot.ChatBubbleTouchListener
import com.education.calmlearn.ui.chatbot.ChatbotBottomSheet
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

    private val destinationsWithoutChatBubble = setOf(
        R.id.onboardingFragment,
        R.id.loginFragment,
        R.id.registerFragment,
        R.id.forgotPasswordFragment,
        R.id.verifyEmailFragment
    )

    private var currentDestinationId = 0
    private var chatBubblePlaced = false
    private var desiredBubbleX = 0f
    private var desiredBubbleY = 0f

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
            currentDestinationId = destination.id
            updateChatBubbleVisibility()
        }

        setupChatBubble()
    }

    private fun setupChatBubble() {
        val density = resources.displayMetrics.density
        binding.chatBubble.setOnTouchListener(
            ChatBubbleTouchListener((BUBBLE_EDGE_MARGIN_DP * density).toInt()) { rememberChatBubblePosition() }
        )
        binding.chatBubble.setOnClickListener { ChatbotBottomSheet.show(supportFragmentManager) }

        ChatBubbleGate.quizInProgress.observe(this) { updateChatBubbleVisibility() }

        binding.root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (chatBubblePlaced) clampChatBubble()
        }
    }

    private fun updateChatBubbleVisibility() {
        val hidden = currentDestinationId in destinationsWithoutChatBubble ||
            ChatBubbleGate.quizInProgress.value == true
        binding.chatBubble.visibility = if (hidden) View.GONE else View.VISIBLE
        if (!hidden && !chatBubblePlaced) {
            binding.chatBubble.post { placeChatBubble() }
        }
    }

    private fun placeChatBubble() {
        val density = resources.displayMetrics.density
        val bubble = binding.chatBubble
        bubble.x = (binding.root.width - bubble.width - BUBBLE_EDGE_MARGIN_DP * density)
        bubble.y = (binding.root.height - bubble.height - BUBBLE_DEFAULT_BOTTOM_DP * density)
        chatBubblePlaced = true
        rememberChatBubblePosition()
        clampChatBubble()
    }

    private fun rememberChatBubblePosition() {
        desiredBubbleX = binding.chatBubble.x
        desiredBubbleY = binding.chatBubble.y
    }

    private fun clampChatBubble() {
        val bubble = binding.chatBubble
        bubble.x = desiredBubbleX.coerceIn(0f, (binding.root.width - bubble.width).toFloat().coerceAtLeast(0f))
        bubble.y = desiredBubbleY.coerceIn(0f, (binding.root.height - bubble.height).toFloat().coerceAtLeast(0f))
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

    private companion object {
        const val BUBBLE_EDGE_MARGIN_DP = 12
        const val BUBBLE_DEFAULT_BOTTOM_DP = 100
    }
}
