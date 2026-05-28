package com.example.beattreat.ui.ArtistaDetalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.FirestoreAlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistaDetalleViewModel @Inject constructor(
    private val firestoreAlbumRepository: FirestoreAlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistaDetalleUIState())
    val uiState: StateFlow<ArtistaDetalleUIState> = _uiState.asStateFlow()

    fun cargarArtista(artistaId: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val rawResult = firestoreAlbumRepository.getAllAlbumsRaw()
            if (rawResult.isSuccess) {
                val albumsMap = rawResult.getOrDefault(emptyMap())

                // Agrupar por artista igual que HomeViewModel
                val artistasAgrupados = albumsMap.entries
                    .groupBy { it.value.artist }
                    .entries
                    .mapIndexed { index, (artista, entries) ->
                        Pair(
                            index + 1,
                            ArtistaDetalleUI(
                                id     = index + 1,
                                nombre = artista,
                                albumes = entries.map { (firestoreId, dto) ->
                                    AlbumArtistaUI(
                                        id        = firestoreId.hashCode(),
                                        nombre    = dto.title,
                                        imagenUrl = dto.coverImage
                                    )
                                }
                            )
                        )
                    }

                val artista = artistasAgrupados.find { it.first == artistaId }?.second

                _uiState.update {
                    it.copy(
                        artista   = artista,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleSeguir() {
        _uiState.update { it.copy(siguiendo = !it.siguiendo) }
    }
}