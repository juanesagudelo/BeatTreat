package com.example.beattreat

import com.example.beattreat.data.repository.AuthRepository
import com.example.beattreat.ui.Login.LoginViewModel
import com.google.common.truth.Truth
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelMockTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel      = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── TEST 1: login exitoso → loginExitoso = true ──────────────────────────
    @Test
    fun login_validCredentials_loginExitosoTrue() = runTest {
        // arrange
        coEvery { authRepository.signIn(any(), any()) } returns Result.success(Unit)
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")
        // act
        viewModel.login()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.loginExitoso).isTrue()
    }

    // ─── TEST 2: login fallido → errorMessage no null ─────────────────────────
    @Test
    fun login_wrongCredentials_errorMessageNotNull() = runTest {
        // arrange
        coEvery { authRepository.signIn(any(), any()) } returns Result.failure(Exception("Correo o contraseña incorrectos"))
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("wrongpass")
        // act
        viewModel.login()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        Truth.assertThat(viewModel.uiState.value.loginExitoso).isFalse()
    }

    // ─── TEST 3: campos vacíos → error sin llamar al repositorio ─────────────
    @Test
    fun login_emptyFields_showsErrorWithoutCallingRepo() = runTest {
        // arrange - no se mockea authRepository porque no debe llamarse
        // act
        viewModel.login()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        Truth.assertThat(viewModel.uiState.value.loginExitoso).isFalse()
    }

    // ─── TEST 4: onEmailChange → estado actualizado ───────────────────────────
    @Test
    fun onEmailChange_updatesState() = runTest {
        // act
        viewModel.onEmailChange("nuevo@email.com")
        // assert
        Truth.assertThat(viewModel.uiState.value.email).isEqualTo("nuevo@email.com")
    }

    // ─── TEST 5: onPasswordChange → estado actualizado ───────────────────────
    @Test
    fun onPasswordChange_updatesState() = runTest {
        // act
        viewModel.onPasswordChange("mipassword")
        // assert
        Truth.assertThat(viewModel.uiState.value.password).isEqualTo("mipassword")
    }

    // ─── TEST 6: login → isLoading false al terminar ──────────────────────────
    @Test
    fun login_afterFinish_isLoadingFalse() = runTest {
        // arrange
        coEvery { authRepository.signIn(any(), any()) } returns Result.success(Unit)
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")
        // act
        viewModel.login()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    // ─── TEST 7: resetLoginExitoso → loginExitoso = false ────────────────────
    @Test
    fun resetLoginExitoso_setsLoginExitosoFalse() = runTest {
        // arrange
        coEvery { authRepository.signIn(any(), any()) } returns Result.success(Unit)
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")
        viewModel.login()
        advanceUntilIdle()
        Truth.assertThat(viewModel.uiState.value.loginExitoso).isTrue()
        // act
        viewModel.resetLoginExitoso()
        // assert
        Truth.assertThat(viewModel.uiState.value.loginExitoso).isFalse()
    }

    // ─── TEST 8: email vacío con password → error ─────────────────────────────
    @Test
    fun login_emptyEmail_showsError() = runTest {
        // arrange
        viewModel.onPasswordChange("123456")
        // act
        viewModel.login()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }
}
