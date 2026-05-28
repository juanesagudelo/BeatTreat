package com.example.beattreat.ui.TopAlbums

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.beattreat.ui.theme.BeatTreatColors

private val Oro    = Color(0xFFFFD700)
private val Plata  = Color(0xFFC0C0C0)
private val Bronce = Color(0xFFCD7F32)

private fun medallaColor(pos: Int) = when (pos) {
    0    -> Oro
    1    -> Plata
    2    -> Bronce
    else -> BeatTreatColors.TextGray
}

// ── Stateful ──────────────────────────────────────────────────────────────────
@Composable
fun TopAlbumsScreen(
    onAlbumClick: (Int) -> Unit = {},
    onBackClick:  () -> Unit    = {},
    modifier:     Modifier      = Modifier,
    viewModel:    TopAlbumsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    TopAlbumsContent(
        uiState        = uiState,
        generos        = viewModel.generos,
        onAlbumClick   = onAlbumClick,
        onGeneroSelect = { viewModel.filtrarPorGenero(it) },
        onBackClick    = onBackClick,
        modifier       = modifier
    )
}

// ── Stateless ─────────────────────────────────────────────────────────────────
@Composable
fun TopAlbumsContent(
    uiState:        TopAlbumsUIState,
    generos:        List<String>,
    onAlbumClick:   (Int) -> Unit,
    onGeneroSelect: (String) -> Unit,
    onBackClick:    () -> Unit,
    modifier:       Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
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
                        text       = "Top Álbumes",
                        color      = Color.White,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text     = "Los mejor calificados por la comunidad",
                        color    = BeatTreatColors.TextGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── Filtro de géneros ──────────────────────────────────────────────────
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(generos) { genero ->
                val seleccionado = genero == uiState.generoSeleccionado
                FilterChip(
                    selected = seleccionado,
                    onClick  = { onGeneroSelect(genero) },
                    label    = { Text(genero, fontSize = 13.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BeatTreatColors.Purple40,
                        selectedLabelColor     = Color.White,
                        containerColor         = BeatTreatColors.SurfaceVariant,
                        labelColor             = BeatTreatColors.TextGray
                    )
                )
            }
        }

        // ── Contenido ─────────────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BeatTreatColors.Purple40)
                }
            }
            uiState.errorMessage != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.errorMessage, color = BeatTreatColors.TextGray)
                }
            }
            uiState.albums.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin álbumes en este género", color = BeatTreatColors.TextGray)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(uiState.albums) { index, album ->
                        TopAlbumItem(
                            album    = album,
                            posicion = index,
                            onClick  = { onAlbumClick(album.firestoreId.hashCode()) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Card de álbum ─────────────────────────────────────────────────────────────
@Composable
fun TopAlbumItem(
    album:    TopAlbumUI,
    posicion: Int,
    onClick:  () -> Unit
) {
    val medalla = medallaColor(posicion)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BeatTreatColors.Surface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "#${posicion + 1}",
                color      = medalla,
                fontSize   = if (posicion < 3) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(8.dp))

        AsyncImage(
            model              = album.coverImage,
            contentDescription = album.titulo,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = album.titulo,
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text     = album.artista,
                color    = BeatTreatColors.TextGray,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text     = album.genero,
                color    = BeatTreatColors.Purple60,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Filled.Star,
                    contentDescription = null,
                    tint               = Oro,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text       = if (album.rating > 0f) "%.1f".format(album.rating) else "—",
                    color      = Color.White,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text     = "${album.totalResenas} reseñas",
                color    = BeatTreatColors.TextGray,
                fontSize = 10.sp
            )
        }
    }
}