package com.education.calmlearn.ui.start

enum class StartDestination {
    ONBOARDING,
    LOGIN,
    HOME
}

object StartDestinationResolver {
    fun resolve(hasCompletedOnboarding: Boolean, isAuthenticated: Boolean): StartDestination = when {
        !hasCompletedOnboarding -> StartDestination.ONBOARDING
        isAuthenticated -> StartDestination.HOME
        else -> StartDestination.LOGIN
    }
}
