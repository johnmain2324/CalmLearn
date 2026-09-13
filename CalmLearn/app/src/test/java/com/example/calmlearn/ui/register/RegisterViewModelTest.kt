@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.calmlearn.ui.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelStore
import com.example.calmlearn.R
import com.example.calmlearn.data.auth.AuthErrorReason
import com.example.calmlearn.data.auth.Gender
import com.example.calmlearn.data.auth.RegisterResult
import com.example.calmlearn.testutil.FakeAuthRepository
import com.example.calmlearn.ui.common.FormUiState
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = RegisterViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidForm() {
        viewModel.onFullNameChanged("Nguyen Van A")
        viewModel.onEmailChanged("a@example.com")
        viewModel.onPasswordChanged("123456")
        viewModel.onConfirmPasswordChanged("123456")
        viewModel.onGenderChanged(Gender.OTHER)
        viewModel.onTermsChanged(true)
    }

    @Test
    fun `no error is shown before any field is touched`() {
        val errors = viewModel.fieldErrors.value!!
        assertNull(errors.fullName)
        assertNull(errors.email)
        assertNull(errors.password)
        assertNull(errors.confirmPassword)
        assertNull(errors.gender)
        assertNull(errors.terms)
    }

    @Test
    fun `field error appears right after that field is edited and clears once corrected`() {
        viewModel.onFullNameChanged("")
        assertEquals(R.string.register_error_fullname, viewModel.fieldErrors.value?.fullName)

        viewModel.onFullNameChanged("Nguyen Van A")
        assertNull(viewModel.fieldErrors.value?.fullName)
    }

    @Test
    fun `blurring an untouched field reveals its error without typing`() {
        assertNull(viewModel.fieldErrors.value?.terms)
        viewModel.onFieldBlurred(RegisterField.TERMS)
        assertEquals(R.string.register_error_terms, viewModel.fieldErrors.value?.terms)
    }

    @Test
    fun `changing the base password re-evaluates a previously touched confirm password error`() {
        viewModel.onConfirmPasswordChanged("abcdef")
        viewModel.onPasswordChanged("abcdef")
        assertNull(viewModel.fieldErrors.value?.confirmPassword)

        viewModel.onPasswordChanged("zzzzzz")
        assertEquals(R.string.register_error_confirm_password, viewModel.fieldErrors.value?.confirmPassword)
    }

    @Test
    fun `submit with an incomplete form reveals every missing or invalid field and does not call the repository`() {
        viewModel.submit()

        val errors = viewModel.fieldErrors.value!!
        assertEquals(R.string.register_error_fullname, errors.fullName)
        assertEquals(R.string.register_error_terms, errors.terms)
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
        assertEquals(0, fakeRepository.registerCallCount)
    }

    @Test
    fun `submitting twice while loading only calls the repository once`() = runTest(testDispatcher) {
        fakeRepository.holdRegister()
        fillValidForm()

        viewModel.submit()
        assertEquals(FormUiState.Loading, viewModel.uiState.value)
        viewModel.submit() // trung lap trong luc dang Loading - phai bi bo qua

        fakeRepository.releaseRegisterGate()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.registerCallCount)
        assertEquals(FormUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `signed in result navigates via Success state`() = runTest(testDispatcher) {
        fakeRepository.registerResult = RegisterResult.SignedIn
        fillValidForm()
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(FormUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `requires verification result does not report Success (must not auto navigate home)`() = runTest(testDispatcher) {
        fakeRepository.registerResult = RegisterResult.RequiresVerification
        fillValidForm()
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(FormUiState.RequiresNextStep, viewModel.uiState.value)
    }

    @Test
    fun `service error maps to Error state`() = runTest(testDispatcher) {
        fakeRepository.registerResult = RegisterResult.Error(AuthErrorReason.NETWORK_ERROR)
        fillValidForm()
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(FormUiState.Error(AuthErrorReason.NETWORK_ERROR), viewModel.uiState.value)
    }

    @Test
    fun `unexpected exception from the repository maps to an error instead of staying stuck loading`() = runTest(testDispatcher) {
        fakeRepository.throwOnNextCall = RuntimeException("boom")
        fillValidForm()
        viewModel.submit()
        assertEquals(FormUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is FormUiState.Error)
    }

    @Test
    fun `stale service error clears as soon as the user edits a field again`() = runTest(testDispatcher) {
        fakeRepository.registerResult = RegisterResult.Error(AuthErrorReason.NETWORK_ERROR)
        fillValidForm()
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is FormUiState.Error)

        viewModel.onEmailChanged("a2@example.com")
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `consumeTerminalState resets Success to Idle so it is not replayed as a second navigation`() = runTest(testDispatcher) {
        fillValidForm()
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(FormUiState.Success, viewModel.uiState.value)

        viewModel.consumeTerminalState()
        assertEquals(FormUiState.Idle, viewModel.uiState.value)

        // Goi lai lan nua (vd Fragment quan sat lai sau khi View duoc tao lai) khong duoc gay loi
        // hay doi trang thai khac Idle.
        viewModel.consumeTerminalState()
        assertEquals(FormUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `cancelling the view model scope prevents a late result from updating state`() = runTest(testDispatcher) {
        fakeRepository.holdRegister()
        fillValidForm()
        viewModel.submit()
        assertEquals(FormUiState.Loading, viewModel.uiState.value)

        // Gia lap nguoi dung roi man hinh (Fragment bi pop khoi back stack -> ViewModel cleared).
        val store = ViewModelStore()
        store.put("register", viewModel)
        store.clear()

        fakeRepository.releaseRegisterGate()
        advanceUntilIdle()

        // Ket qua den tre khong duoc cap nhat trang thai nua vi coroutine da bi huy cung ViewModel.
        assertEquals(FormUiState.Loading, viewModel.uiState.value)
    }
}
