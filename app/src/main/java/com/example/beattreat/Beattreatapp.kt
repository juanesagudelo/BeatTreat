package com.example.beattreat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.beattreat.navigation.AppNavegacion
import com.example.beattreat.navigation.Screen
import com.example.beattreat.ui.components.NotificationPermissionHandler

@Composable
fun BeatTreatApp() {
    val navController = rememberNavController()
    val currentRoute  by navController.currentBackStackEntryAsState()
    val rutaActual    = currentRoute?.destination?.route

    val pantallasOcultas = listOf(
        Screen.Login.route,
        Screen.Registro.route
    )
    val mostrarBottomBar = rutaActual !in pantallasOcultas

    NotificationPermissionHandler(
        onPermissionGranted = {},
        onPermissionDenied  = {}
    )

    Scaffold(
        modifier  = Modifier.fillMaxSize(),
        bottomBar = {
            if (mostrarBottomBar) {
                BottomNavigationBar(
                    rutaActual           = rutaActual ?: "",
                    onHomeClick          = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onBibliotecaClick    = {
                        navController.navigate(Screen.Biblioteca.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onDescubreClick      = {
                        navController.navigate(Screen.Descubre.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onChatClick          = { navController.navigate(Screen.Grupos.route) },
                    onFeedSiguiendoClick = {
                        navController.navigate("feed_siguiendo") {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavegacion(
            navController = navController,
            modifier      = Modifier.padding(innerPadding)
        )
    }
}