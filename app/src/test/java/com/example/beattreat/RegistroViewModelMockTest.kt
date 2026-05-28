package com.example.beattreat

import com.example.beattreat.data.repository.AuthRepository
import com.example.beattreat.data.repository.FirestoreUserRepository
import com.example.beattreat.ui.Registro.RegistroViewModel
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
class RegistroViewModelMockTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreUserRepository: FirestoreUserRepository
    private lateinit var viewModel: RegistroViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository         = mockk()
        firestoreUserRepository = mockk()
        viewModel              = RegistroViewModel(authRepository, firestoreUserRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun llenarFormulario(password: String = "123456") {
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange(password)
        viewModel.onNombreChange("Santiago García")
        viewModel.onUsernameChange("santiago_garcia")
        viewModel.onCountryChange("Colombia")
        viewModel.onBioChange("Fan del reggaetón")
    }

    // ─── TEST 1: contraseña corta → error sin llamar al repo ─────────────────
    @Test
    fun registrar_shortPassword_showsError() = runTest {
        // arrange
        llenarFormulario(password = "1234")
        // act
        viewModel.registrar()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        Truth.assertThat(viewModel.uiState.value.errorMessage).contains("6")
        Truth.assertThat(viewModel.uiState.value.registroExitoso).isFalse()
    }

    // ─── TEST 2: registro exitoso → registroExitoso = true ───────────────────
    @Test
    fun registrar_validData_registroExitosoTrue() = runTest {
        // arrange
        coEvery { authRepository.signUp(any(), any()) } returns Result.success(Unit)
        coEvery { firestoreUserRepository.registerUser(any(), any(), any(), any()) } returns Result.success(Unit)
        llenarFormulario()
        // act
        viewModel.registrar()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.registroExitoso).isTrue()
    }

    // ─── TEST 3: campos vacíos → error ────────────────────────────────────────
    @Test
    fun registrar_emptyFields_showsError() = runTest {
        // act
        viewModel.registrar()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        Truth.assertThat(viewModel.uiState.value.registroExitoso).isFalse()
    }

    // ─── TEST 4: auth falla → errorMessage no null ────────────────────────────
    @Test
    fun registrar_authFails_showsError() = runTest {
        // arrange
        coEvery { authRepository.signUp(any(), any()) } returns Result.failure(Exception("Este correo ya está registrado"))
        llenarFormulario()
        // act
        viewModel.registrar()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        Truth.assertThat(viewModel.uiState.value.registroExitoso).isFalse()
    }

    // ─── TEST 5: onNombreChange → estado actualizado ──────────────────────────
    @Test
    fun onNombreChange_updatesState() = runTest {
        // act
        viewModel.onNombreChange("Valentina López")
        // assert
        Truth.assertThat(viewModel.uiState.value.nombre).isEqualTo("Valentina López")
    }

    // ─── TEST 6: nombre vacío → error ─────────────────────────────────────────
    @Test
    fun registrar_emptyName_showsError() = runTest {
        // arrange
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")
        viewModel.onUsernameChange("username_test")
        // act
        viewModel.registrar()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }

    // ─── TEST 7: isLoading false después del registro ─────────────────────────
    @Test
    fun registrar_afterFinish_isLoadingFalse() = runTest {
        // arrange
        coEvery { authRepository.signUp(any(), any()) } returns Result.success(Unit)
        coEvery { firestoreUserRepository.registerUser(any(), any(), any(), any()) } returns Result.success(Unit)
        llenarFormulario()
        // act
        viewModel.registrar()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    // ─── TEST 8: resetRegistroExitoso → registroExitoso = false ──────────────
    @Test
    fun resetRegistroExitoso_setsRegistroExitosoFalse() = runTest {
        // arrange
        coEvery { authRepository.signUp(any(), any()) } returns Result.success(Unit)
        coEvery { firestoreUserRepository.registerUser(any(), any(), any(), any()) } returns Result.success(Unit)
        llenarFormulario()
        viewModel.registrar()
        advanceUntilIdle()
        Truth.assertThat(viewModel.uiState.value.registroExitoso).isTrue()
        // act
        viewModel.resetRegistroExitoso()
        // assert
        Truth.assertThat(viewModel.uiState.value.registroExitoso).isFalse()
    }
}
