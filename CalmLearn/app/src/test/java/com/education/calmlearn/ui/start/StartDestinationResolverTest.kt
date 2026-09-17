package com.education.calmlearn.ui.start

import org.junit.Assert.assertEquals
import org.junit.Test

class StartDestinationResolverTest {

    @Test
    fun `has not seen onboarding goes to onboarding regardless of session`() {
        assertEquals(
            StartDestination.ONBOARDING,
            StartDestinationResolver.resolve(hasCompletedOnboarding = false, isAuthenticated = false)
        )
        assertEquals(
            StartDestination.ONBOARDING,
            StartDestinationResolver.resolve(hasCompletedOnboarding = false, isAuthenticated = true)
        )
    }

    @Test
    fun `seen onboarding without a valid session goes to login`() {
        assertEquals(
            StartDestination.LOGIN,
            StartDestinationResolver.resolve(hasCompletedOnboarding = true, isAuthenticated = false)
        )
    }

    @Test
    fun `seen onboarding with a valid session goes straight to home`() {
        assertEquals(
            StartDestination.HOME,
            StartDestinationResolver.resolve(hasCompletedOnboarding = true, isAuthenticated = true)
        )
    }
}
