@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.education.calmlearn.ui.verifyemail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelStore
import com.education.calmlearn.data.auth.AuthErrorReason
import com.education.calmlearn.data.auth.AuthResult
import com.education.calmlearn.data.auth.Gender
import com.education.calmlearn.data.auth.ProfileResult
import com.education.calmlearn.data.auth.UserProfile
import com.education.calmlearn.data.auth.VerificationCheckResult
import com.education.calmlearn.testutil.FakeAuthRepository
import com.education.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VerifyEmailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: VerifyEmailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        fakeRepository.sessionEmail = "a@example.com"
        viewModel = VerifyEmailViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun loadedProfile(gender: Gender? = Gender.OTHER) = ProfileResult.Loaded(
        UserProfile(uid = "uid-1", fullName = "Nguyen Van A", email = "a@example.com", gender = gender)
    )

    @Test
    fun `profile missing routes to the profile completion form`() = runTest(testDispatcher) {
        fakeRepository.profile = ProfileResult.Missing
        viewModel.initialize(autoSend = true)
        advanceUntilIdle()
        assertTrue(viewModel.mode.value is VerifyEmailMode.ProfileForm)
        // Khong duoc tu goi gui xac minh khi chua co ho so.
        assertEquals(0, fakeRepository.resendCallCount)
    }

    @Test
    fun `profile loaded with autoSend true sends verification exactly once`() = runTest(testDispatcher) {
        fakeRepository.profile = loadedProfile()
        viewModel.initialize(autoSend = true)
        advanceUntilIdle()
        assertTrue(viewModel.mode.value is VerifyEmailMode.Verify)
        assertEquals(1, fakeRepository.resendCallCount)
    }

    @Test
    fun `profile loaded with autoSend false does not send verification automatically`() = runTest(testDispatcher) {
        fakeRepository.profile = loadedProfile()
        viewModel.initialize(autoSend = false)
        advanceUntilIdle()
        assertTrue(viewModel.mode.value is VerifyEmailMode.Verify)
        assertEquals(0, fakeRepository.resendCallCount)
    }

    @Test
    fun `read error on initial load is distinct from a missing profile - shows an error, not the profile form`() = runTest(testDispatcher) {
        fakeRepository.profile = ProfileResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        viewModel.initialize(autoSend = true)
        advanceUntilIdle()
        assertTrue(viewModel.mode.value is VerifyEmailMode.Verify)
        assertEquals(FormUiState.Error(AuthErrorReason.NETWORK_ERROR), viewModel.uiState.value)
        assertEquals(0, fakeRepository.resendCallCount)
    }

    @Test
    fun `submitting the profile form saves the profile then sends verification automatically`() = runTest(testDispatcher) {
        fakeRepository.profile = ProfileResult.Missing
        viewModel.initialize(autoSend = false)
        advanceUntilIdle()

        viewModel.onFullNameChanged("Nguyen Van B")
        viewModel.onGenderChanged(Gender.FEMALE)
        viewModel.submitProfile()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.completeProfileCallCount)
        assertEquals(1, fakeRepository.resendCallCount)
        assertTrue(viewModel.mode.value is VerifyEmailMode.Verify)
    }

    @Test
    fun `profile save failure surfaces an error and can be retried without duplicating the call`() = runTest(testDispatcher) {
        fakeRepository.profile = ProfileResult.Missing
        fakeRepository.completeProfileResult = AuthResult.Error(AuthErrorReason.PROFILE_SAVE_FAILED)
        viewModel.initialize(autoSend = false)
        advanceUntilIdle()

        viewModel.onFullNameChanged("Nguyen Van B")
        viewModel.onGenderChanged(Gender.FEMALE)
        viewModel.submitProfile()
        advanceUntilIdle()

        assertEquals(FormUiState.Error(AuthErrorReason.PROFILE_SAVE_FAILED), viewModel.uiState.value)
        assertEquals(1, fakeRepository.completeProfileCallCount)
        assertEquals(0, fakeRepository.resendCallCount)

        // Nguoi dung thu lai sau khi sua - khong bi khoa, va khong goi trung trong luc dang Loading.
        fakeRepository.completeProfileResult = AuthResult.Success
        viewModel.submitProfile()
        viewModel.submitProfile() // trung lap trong luc dang Loading - phai bi bo qua
        advanceUntilIdle()

        assertEquals(2, fakeRepository.completeProfileCallCount)
        assertEquals(1, fakeRepository.resendCallCount)
    }

    @Test
    fun `resend failure then a manual resend can succeed`() = runTest(testDispatcher) {
        fakeRepository.profile = loadedProfile()
        fakeRepository.resendResult = AuthResult.Error(AuthErrorReason.NETWORK_ERROR)
        viewModel.initialize(autoSend = true)
        advanceUntilIdle()
        assertEquals(FormUiState.Error(AuthErrorReason.NETWORK_ERROR), viewModel.uiState.value)

        fakeRepository.resendResult = AuthResult.Success
        viewModel.resend()
        advanceUntilIdle()
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
        assertEquals(2, fakeRepository.resendCallCount)
    }

    @Test
    fun `checkAgain reporting verified maps to Success`() = runTest(testDispatcher) {
        fakeRepository.profile = loadedProfile()
        fakeRepository.refreshResult = VerificationCheckResult.VERIFIED
        viewModel.initialize(autoSend = false)
        advanceUntilIdle()

        viewModel.checkAgain()
        advanceUntilIdle()
        assertEquals(FormUiState.Success, viewModel.uiState.value)

        viewModel.consumeTerminalState()
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `checkAgain reporting not yet verified stays on the screen instead of failing silently`() = runTest(testDispatcher) {
        fakeRepository.profile = loadedProfile()
        fakeRepository.refreshResult = VerificationCheckResult.NOT_YET_VERIFIED
        viewModel.initialize(autoSend = false)
        advanceUntilIdle()

        viewModel.checkAgain()
        advanceUntilIdle()
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `duplicate checkAgain calls while loading only check once`() = runTest(testDispatcher) {
        fakeRepository.profile = loadedProfile()
        viewModel.initialize(autoSend = false)
        advanceUntilIdle()

        viewModel.checkAgain()
        assertEquals(FormUiState.Loading, viewModel.uiState.value)
        viewModel.checkAgain() // trung lap trong luc dang Loading - phai bi bo qua
        advanceUntilIdle()

        assertEquals(1, fakeRepository.refreshCallCount)
    }

    @Test
    fun `abandon cancels the pending session on the repository`() {
        viewModel.abandon()
        assertEquals(1, fakeRepository.cancelPendingSessionCallCount)
    }

    @Test
    fun `cancelling the view model scope prevents a late resend result from updating state`() = runTest(testDispatcher) {
        fakeRepository.profile = loadedProfile()
        viewModel.initialize(autoSend = false)
        advanceUntilIdle()

        viewModel.resend()
        assertEquals(FormUiState.Loading, viewModel.uiState.value)

        val store = ViewModelStore()
        store.put("verify", viewModel)
        store.clear()

        advanceUntilIdle()
        assertEquals(FormUiState.Loading, viewModel.uiState.value)
    }
}
