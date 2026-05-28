package com.example.beattreat.ui.Estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beattreat.ui.theme.BeatTreatColors

private val Oro = Color(0xFFFFD700)

// ── Stateful ──────────────────────────────────────────────────────────────────
@Composable
fun EstadisticasScreen(
    onBackClick: () -> Unit = {},
    modifier:    Modifier   = Modifier,
    viewModel:   EstadisticasViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    EstadisticasContent(
        uiState     = uiState,
        onRetry     = { viewModel.cargarEstadisticas() },
        onBackClick = onBackClick,
        modifier    = modifier
    )
}

// ── Stateless ─────────────────────────────────────────────────────────────────
@Composable
fun EstadisticasContent(
    uiState:     EstadisticasUIState,
    onRetry:     () -> Unit,
    onBackClick: () -> Unit,
    modifier:    Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header con flecha ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(BeatTreatColors.PurpleDark, MaterialTheme.colorScheme.background)
                    )
                )
                .padding(horizontal = 4.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector        = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint               = Color.White
                    )
                }
                Column {
                    Text(
                        text       = "Mis Estadísticas",
                        color      = Color.White,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text     = "Tu actividad en BeatTreat",
                        color    = BeatTreatColors.TextGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

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

            uiState.totalResenas == 0 -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text     = "Aún no tienes reseñas.\n¡Empieza a reseñar álbumes!",
                        color    = BeatTreatColors.TextGray,
                        fontSize = 15.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    // ── Fila de métricas principales ──────────────────────────
                    item {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricaCard(
                                titulo   = "Reseñas",
                                valor    = "${uiState.totalResenas}",
                                icono    = Icons.Filled.RateReview,
                                color    = BeatTreatColors.Purple40,
                                modifier = Modifier.weight(1f)
                            )
                            MetricaCard(
                                titulo   = "Rating ⌀",
                                valor    = "%.1f".format(uiState.ratingPromedio),
                                icono    = Icons.Filled.Star,
                                color    = Oro,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ── Fila max/min ──────────────────────────────────────────
                    item {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricaCard(
                                titulo   = "Mayor rating",
                                valor    = "%.1f".format(uiState.ratingMaximo),
                                icono    = Icons.Filled.TrendingUp,
                                color    = BeatTreatColors.Success,
                                modifier = Modifier.weight(1f)
                            )
                            MetricaCard(
                                titulo   = "Menor rating",
                                valor    = "%.1f".format(uiState.ratingMinimo),
                                icono    = Icons.Filled.TrendingDown,
                                color    = BeatTreatColors.Error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ── Álbum más reciente ────────────────────────────────────
                    if (uiState.albumMasReciente.isNotBlank()) {
                        item {
                            InfoCard(
                                titulo    = "Última reseña",
                                subtitulo = uiState.albumMasReciente,
                                icono     = Icons.Filled.MusicNote
                            )
                        }
                    }

                    // ── Géneros favoritos ─────────────────────────────────────
                    if (uiState.generosFavoritos.isNotEmpty()) {
                        item {
                            GenerosFavoritosCard(generos = uiState.generosFavoritos)
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Tarjeta de métrica simple ─────────────────────────────────────────────────
@Composable
fun MetricaCard(
    titulo:   String,
    valor:    String,
    icono:    ImageVector,
    color:    Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BeatTreatColors.Surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text       = valor,
            color      = Color.White,
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text     = titulo,
            color    = BeatTreatColors.TextGray,
            fontSize = 12.sp
        )
    }
}

// ── Tarjeta de info simple ────────────────────────────────────────────────────
@Composable
fun InfoCard(
    titulo:    String,
    subtitulo: String,
    icono:     ImageVector,
    modifier:  Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BeatTreatColors.Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            tint               = BeatTreatColors.Purple60,
            modifier           = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = titulo,    color = BeatTreatColors.TextGray, fontSize = 12.sp)
            Text(
                text       = subtitulo,
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Tarjeta de géneros favoritos ──────────────────────────────────────────────
@Composable
fun GenerosFavoritosCard(
    generos:  List<GeneroEstadisticaUI>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BeatTreatColors.Surface)
            .padding(16.dp)
    ) {
        Text(
            text       = "Géneros favoritos",
            color      = Color.White,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        generos.forEach { genero ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = genero.nombre,   color = Color.White,              fontSize = 13.sp)
                    Text(
                        text     = "${genero.cantidad} reseñas · ${"%.0f".format(genero.porcentaje)}%",
                        color    = BeatTreatColors.TextGray,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BeatTreatColors.SurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(genero.porcentaje / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BeatTreatColors.Purple40, BeatTreatColors.Purple60)
                                )
                            )
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}