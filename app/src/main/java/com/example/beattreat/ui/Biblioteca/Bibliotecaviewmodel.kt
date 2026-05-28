package com.example.beattreat.ui.Biblioteca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.FirestoreAlbumRepository
import com.example.beattreat.data.repository.FirestoreUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val firestoreAlbumRepository: FirestoreAlbumRepository,
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BibliotecaUIState())
    val uiState: StateFlow<BibliotecaUIState> = _uiState.asStateFlow()

    init {
        cargarBiblioteca()
    }

    private fun cargarBiblioteca() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val albumsDeferred    = async { firestoreAlbumRepository.getAllAlbumsRaw() }
            val favoritosDeferred = async { firestoreUserRepository.getFavorites() }

            val albumsResult    = albumsDeferred.await()
            val favoritosResult = favoritosDeferred.await()

            val albumsMap      = albumsResult.getOrDefault(emptyMap())
            val totalAlbumes   = albumsMap.size
            val totalArtistas  = albumsMap.values.groupBy { it.artist }.size
            val totalFavoritos = favoritosResult.getOrDefault(emptyList()).size

            val playlists = listOf(
                PlaylistUI(
                    id          = 1,
                    nombre      = "Álbumes favoritos",
                    descripcion = "$totalFavoritos álbumes",
                    imagenUrl   = ""
                ),
                PlaylistUI(
                    id          = 2,
                    nombre      = "Mi colección",
                    descripcion = "$totalAlbumes álbumes · $totalArtistas artistas",
                    imagenUrl   = ""
                )
            )

            _uiState.update {
                it.copy(
                    cancionesGuardadas = CancionGuardadaUI(1, "Canciones guardadas", totalAlbumes * 10, ""),
                    artistas           = ArtistaUI(1, "Artistas", totalArtistas, ""),
                    albumes            = AlbumUI(1, "Álbumes", totalAlbumes, ""),
                    playlists          = playlists,
                    isLoading          = false
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun playlistsFiltradas(): List<PlaylistUI> {
        val query = _uiState.value.searchQuery.trim().lowercase()
        return if (query.isEmpty()) {
            _uiState.value.playlists
        } else {
            _uiState.value.playlists.filter { it.nombre.lowercase().contains(query) }
        }
    }
}