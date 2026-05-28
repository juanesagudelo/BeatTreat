package com.example.beattreat

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.beattreat.data.datasource.AuthRemoteDataSource
import com.example.beattreat.data.repository.AuthRepository
import com.example.beattreat.ui.Login.LoginViewModel
import com.google.common.truth.Truth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelIntegrationTest {

    private val auth           = Firebase.auth
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        try {
            auth.useEmulator("10.0.2.2", 9099)
        } catch (e: Exception) { }

        val authDataSource = AuthRemoteDataSource(auth)
        val authRepository = AuthRepository(authDataSource)
        viewModel          = LoginViewModel(authRepository)

        try {
            auth.createUserWithEmailAndPassword("test@beattreat.com", "123456").await()
        } catch (e: Exception) {
            Log.d("TAG", "Usuario ya existe: ${e.message}")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        auth.signOut()
    }

    // ─── TEST 1: login con credenciales correctas → loginExitoso true ─────────
    @Test
    fun login_correctCredentials_loginExitosoTrue() = runTest {
        // arrange
        viewModel.onEmailChange("test@beattreat.com")
        viewModel.onPasswordChange("123456")
        // act
        viewModel.login()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.loginExitoso).isTrue()
    }

    // ─── TEST 2: login con contraseña incorrecta → error ──────────────────────
    @Test
    fun login_wrongPassword_errorMessageNotNull() = runTest {
        // arrange
        viewModel.onEmailChange("test@beattreat.com")
        viewModel.onPasswordChange("wrongpass")
        // act
        viewModel.login()
        advanceUntilIdle()
        // assert
        Truth.assertThat(viewModel.uiState.value.loginExitoso).isFalse()
        Truth.assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }
}
