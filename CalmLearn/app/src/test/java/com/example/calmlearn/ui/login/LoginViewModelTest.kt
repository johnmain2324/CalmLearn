@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.calmlearn.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.calmlearn.R
import com.example.calmlearn.data.auth.AuthErrorReason
import com.example.calmlearn.data.auth.AuthResult
import com.example.calmlearn.testutil.FakeAuthRepository
import com.example.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = LoginViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `no error is shown before any field is touched`() {
        assertNull(viewModel.fieldErrors.value?.email)
        assertNull(viewModel.fieldErrors.value?.password)
    }

    @Test
    fun `field error appears after edit and clears once corrected`() {
        viewModel.onEmailChanged("not-an-email")
        assertEquals(R.string.login_error_email, viewModel.fieldErrors.value?.email)

        viewModel.onEmailChanged("a@example.com")
        assertNull(viewModel.fieldErrors.value?.email)
    }

    @Test
    fun `submit with invalid data does not call the repository`() {
        viewModel.submit()
        assertEquals(0, fakeRepository.loginCallCount)
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `submitting twice while loading only calls the repository once`() = runTest(testDispatcher) {
        fakeRepository.holdLogin()
        viewModel.onEmailChanged("a@example.com")
        viewModel.onPasswordChanged("secret")

        viewModel.submit()
        assertEquals(FormUiState.Loading, viewModel.uiState.value)
        viewModel.submit()

        fakeRepository.releaseLoginGate()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.loginCallCount)
    }

    @Test
    fun `valid credentials that the service accepts report Success once`() = runTest(testDispatcher) {
        fakeRepository.loginResult = AuthResult.Success
        viewModel.onEmailChanged("a@example.com")
        viewModel.onPasswordChanged("secret")
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(FormUiState.Success, viewModel.uiState.value)

        viewModel.consumeTerminalState()
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `a correctly formatted request is not automatically treated as authenticated`() = runTest(testDispatcher) {
        // Dinh dang dung khong co nghia da xac thuc thanh cong - dich vu co the tra ve sai thong tin.
        fakeRepository.loginResult = AuthResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        viewModel.onEmailChanged("a@example.com")
        viewModel.onPasswordChanged("wrong-password")
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(FormUiState.Error(AuthErrorReason.INVALID_CREDENTIALS), viewModel.uiState.value)
    }

    @Test
    fun `unexpected exception maps to an error instead of staying stuck loading`() = runTest(testDispatcher) {
        fakeRepository.throwOnNextCall = RuntimeException("boom")
        viewModel.onEmailChanged("a@example.com")
        viewModel.onPasswordChanged("secret")
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is FormUiState.Error)
    }

    @Test
    fun `stale service error clears as soon as the user edits a field again`() = runTest(testDispatcher) {
        fakeRepository.loginResult = AuthResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        viewModel.onEmailChanged("a@example.com")
        viewModel.onPasswordChanged("wrong")
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is FormUiState.Error)

        viewModel.onPasswordChanged("wrong2")
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
    }
}
