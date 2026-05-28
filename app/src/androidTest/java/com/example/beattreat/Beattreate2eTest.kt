package com.example.beattreat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BeatTreatE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        // Esperar a que la app cargue
        composeTestRule.waitForIdle()
    }

    // E2E 1: Usuario nuevo intenta registrarse con contraseña inválida (1234),
    //        verifica el mensaje de error, corrige la contraseña a 123456
    //        y se registra exitosamente.

    @Test
    fun e2e_newUser_wrongPassword_showsError_thenRegistersSuccessfully() {

        // ── Ir a la pantalla de Registro tocando "Sign Up" ────────────────────
        composeTestRule
            .onNodeWithText("Sign Up")
            .performClick()
        composeTestRule.waitForIdle()

        // ── Llenar formulario ─────────────────────────────────────────────────
        composeTestRule
            .onNodeWithTag("registro_nombre")
            .performTextInput("Santiago García")

        composeTestRule
            .onNodeWithTag("registro_username")
            .performTextInput("santiago_e2e_test")

        composeTestRule
            .onNodeWithTag("registro_email")
            .performTextInput("santiago_e2e@beattreat.com")

        // ── Ingresar contraseña corta (inválida) ──────────────────────────────
        composeTestRule
            .onNodeWithTag("registro_password")
            .performTextInput("1234")

        // ── Intentar registrarse ──────────────────────────────────────────────
        composeTestRule
            .onNodeWithTag("registro_btn")
            .performClick()
        composeTestRule.waitForIdle()

        // ── Verificar mensaje de error por contraseña corta ───────────────────
        composeTestRule
            .onNodeWithTag("registro_error")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("La contraseña debe tener al menos 6 caracteres")
            .assertIsDisplayed()

        // ── Corregir contraseña ───────────────────────────────────────────────
        composeTestRule
            .onNodeWithTag("registro_password")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("registro_password")
            .performTextInput("123456")

        // ── Registrarse con contraseña correcta ───────────────────────────────
        composeTestRule
            .onNodeWithTag("registro_btn")
            .performClick()

        composeTestRule.waitForIdle()

        // ── Verificar que ya no aparece el error de contraseña ────────────────
        composeTestRule
            .onNodeWithText("La contraseña debe tener al menos 6 caracteres")
            .assertDoesNotExist()
    }


    // E2E 2: Usuario ya registrado hace login, verifica que los campos
    //        de login funcionan correctamente y que el error aparece
    //        cuando la contraseña es incorrecta.


    @Test
    fun e2e_registeredUser_wrongPassword_showsError_thenLoginSuccessfully() {

        // ── Verificar que estamos en la pantalla de Login ─────────────────────
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsDisplayed()

        // ── Ingresar email correcto pero contraseña incorrecta ────────────────
        composeTestRule
            .onNodeWithTag("login_email")
            .performTextInput("test@beattreat.com")

        composeTestRule
            .onNodeWithTag("login_password")
            .performTextInput("wrongpass")

        composeTestRule
            .onNodeWithTag("login_btn")
            .performClick()

        composeTestRule.waitForIdle()

        // ── Verificar que aparece mensaje de error ────────────────────────────
        composeTestRule
            .onNodeWithTag("login_error")
            .assertIsDisplayed()

        // ── Corregir contraseña ───────────────────────────────────────────────
        composeTestRule
            .onNodeWithTag("login_password")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("login_password")
            .performTextInput("123456")

        // ── Intentar login de nuevo ───────────────────────────────────────────
        composeTestRule
            .onNodeWithTag("login_btn")
            .performClick()

        composeTestRule.waitForIdle()

        // ── Verificar que el error de contraseña ya no aparece ────────────────
        composeTestRule
            .onNodeWithTag("login_error")
            .assertDoesNotExist()
    }
}
