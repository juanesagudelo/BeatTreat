package com.example.beattreat.ui.GeneroDetalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.FirestoreAlbumRepository
import com.example.beattreat.ui.Descubre.DescubreData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneroDetalleViewModel @Inject constructor(
    private val firestoreAlbumRepository: FirestoreAlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneroDetalleUIState())
    val uiState: StateFlow<GeneroDetalleUIState> = _uiState.asStateFlow()

    fun cargarGenero(generoId: Int) {
        val genero = DescubreData.generos.find { it.id == generoId } ?: return
        _uiState.update { it.copy(isLoading = true, nombre = genero.nombre, colorFondo = genero.colorChip) }
        viewModelScope.launch {
            val rawResult = firestoreAlbumRepository.getAllAlbumsRaw()
            if (rawResult.isSuccess) {
                val albumsMap = rawResult.getOrDefault(emptyMap())
                val filtrados = albumsMap.entries
                    .filter { it.value.genre.lowercase().contains(genero.nombre.lowercase()) }
                    .map { (firestoreId, dto) ->
                        AlbumGeneroUI(
                            id      = firestoreId.hashCode(),
                            nombre  = dto.title,
                            artista = dto.artist
                        )
                    }
                _uiState.update { it.copy(albumes = filtrados, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun cargarPorCategoria(categoriaId: Int) {
        val categoria = DescubreData.categorias.find { it.id == categoriaId } ?: return
        _uiState.update { it.copy(isLoading = true, nombre = categoria.nombre.replace("\n", " "), colorFondo = categoria.colorFondo) }
        viewModelScope.launch {
            val rawResult = firestoreAlbumRepository.getAllAlbumsRaw()
            if (rawResult.isSuccess) {
                val albumsMap = rawResult.getOrDefault(emptyMap())
                val albumes = albumsMap.entries.take(6).map { (firestoreId, dto) ->
                    AlbumGeneroUI(
                        id      = firestoreId.hashCode(),
                        nombre  = dto.title,
                        artista = dto.artist
                    )
                }
                _uiState.update { it.copy(albumes = albumes, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}