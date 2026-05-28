package com.example.beattreat.ui.MapaResenas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.beattreat.ui.theme.BeatTreatColors
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private val Oro = Color(0xFFFFD700)

@Composable
fun MapaResenasScreen(
    onResenaClick: (resenaId: String, albumId: String) -> Unit = { _, _ -> },
    onBackClick:   () -> Unit = {},
    modifier:      Modifier = Modifier,
    viewModel:     MapaResenasViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> tienePermisoUbicacion = isGranted }

    LaunchedEffect(Unit) {
        if (!tienePermisoUbicacion) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    MapaResenasContent(
        uiState               = uiState,
        tienePermisoUbicacion = tienePermisoUbicacion,
        context               = context,
        onRetry               = { viewModel.cargarResenas() },
        onResenaClick         = onResenaClick,
        onBackClick           = onBackClick,
        modifier              = modifier
    )
}

@Composable
fun MapaResenasContent(
    uiState:               MapaResenasUIState,
    tienePermisoUbicacion: Boolean,
    context:               Context,
    onRetry:               () -> Unit,
    onResenaClick:         (resenaId: String, albumId: String) -> Unit,
    onBackClick:           () -> Unit = {},
    modifier:              Modifier = Modifier
) {
    var resenaSeleccionada by remember { mutableStateOf<ResenaMapaUI?>(null) }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.711, -74.072), 5f)
    }

    Box(modifier = modifier.fillMaxSize()) {

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BeatTreatColors.Purple40)
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(uiState.errorMessage, color = BeatTreatColors.TextGray)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors  = ButtonDefaults.buttonColors(containerColor = BeatTreatColors.Purple40)
                    ) { Text("Reintentar", color = Color.White) }
                }
            }

            else -> {
                // ── Mapa sin modo oscuro ──────────────────────────────────────
                GoogleMap(
                    modifier            = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    properties          = MapProperties(
                        isMyLocationEnabled = tienePermisoUbicacion
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled     = true,
                        compassEnabled          = true
                    ),
                    onMapLoaded = {
                        if (tienePermisoUbicacion) {
                            try {
                                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                                fusedClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        cameraState.move(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(it.latitude, it.longitude), 13f
                                            )
                                        )
                                    }
                                }
                            } catch (e: SecurityException) { }
                        }
                    }
                ) {
                    uiState.resenas.forEach { resena ->
                        Marker(
                            state   = MarkerState(position = LatLng(resena.latitud, resena.longitud)),
                            title   = resena.albumNombre,
                            snippet = "${resena.autorNombre} · ⭐ ${"%.1f".format(resena.calificacion)}",
                            onClick = { resenaSeleccionada = resena; false }
                        )
                    }
                }

                // ── Header negro con flecha y título ─────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(BeatTreatColors.Background.copy(alpha = 0.85f))
                        .pointerInput(Unit) {}
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint               = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = "Reseñas en el mapa",
                            color      = Color.White,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text     = if (uiState.resenas.isEmpty())
                                "No hay reseñas en las últimas 24 horas"
                            else
                                "Últimas 24 horas · ${uiState.resenas.size} reseñas",
                            color    = BeatTreatColors.TextGray,
                            fontSize = 12.sp
                        )
                    }
                }

                // ── Botón ubicación — blanco, mismo estilo que zoom, encima ──
                if (tienePermisoUbicacion) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 11.dp, bottom = 136.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                try {
                                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                                    fusedClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            cameraState.move(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(it.latitude, it.longitude), 15f
                                                )
                                            )
                                        }
                                    }
                                } catch (e: SecurityException) { }
                            }
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.MyLocation,
                                contentDescription = "Mi ubicación",
                                tint               = Color(0xFF666666),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── Card del review seleccionado ──────────────────────────────
                resenaSeleccionada?.let { resena ->
                    ResenaMapaCard(
                        resena      = resena,
                        onCerrar    = { resenaSeleccionada = null },
                        onVerResena = { onResenaClick(resena.firestoreDocId, resena.albumId) },
                        modifier    = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ResenaMapaCard(
    resena:      ResenaMapaUI,
    onCerrar:    () -> Unit,
    onVerResena: () -> Unit,
    modifier:    Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = BeatTreatColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model              = resena.autorFotoUrl.ifBlank { null },
                    contentDescription = resena.autorNombre,
                    modifier           = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BeatTreatColors.SurfaceVariant)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = resena.autorNombre,  color = Color.White,              fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = resena.autorUsuario, color = BeatTreatColors.TextGray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Oro, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(text = "%.1f".format(resena.calificacion), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BeatTreatColors.SurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Text(text = "💿 ${resena.albumNombre}", color = BeatTreatColors.Purple60, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(text = resena.texto, color = BeatTreatColors.OnBackground, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(text = resena.fecha, color = BeatTreatColors.TextGray, fontSize = 11.sp)
                Row {
                    TextButton(onClick = onVerResena) { Text("Ver reseña", color = BeatTreatColors.Purple60, fontSize = 12.sp) }
                    TextButton(onClick = onCerrar)    { Text("Cerrar",     color = BeatTreatColors.TextGray,  fontSize = 12.sp) }
                }
            }
        }
    }
}