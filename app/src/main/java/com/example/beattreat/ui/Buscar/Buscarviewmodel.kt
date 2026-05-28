package com.example.beattreat.ui.Buscar

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
class BuscarViewModel @Inject constructor(
    private val firestoreAlbumRepository: FirestoreAlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuscarUIState())
    val uiState: StateFlow<BuscarUIState> = _uiState.asStateFlow()

    private var todosLosAlbumes: List<AlbumBuscarUI> = emptyList()
    private var todosLosArtistas: List<ArtistaBuscarUI> = emptyList()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            val rawResult = firestoreAlbumRepository.getAllAlbumsRaw()
            if (rawResult.isSuccess) {
                val albumsMap = rawResult.getOrDefault(emptyMap())

                todosLosAlbumes = albumsMap.entries.map { (firestoreId, dto) ->
                    AlbumBuscarUI(
                        id      = firestoreId.hashCode(),
                        nombre  = dto.title,
                        artista = dto.artist
                    )
                }

                todosLosArtistas = albumsMap.values
                    .groupBy { it.artist }
                    .keys
                    .mapIndexed { index, nombre ->
                        ArtistaBuscarUI(id = index + 1, nombre = nombre)
                    }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        buscar(query)
    }

    private fun buscar(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(resultadosAlbumes = emptyList(), resultadosArtistas = emptyList()) }
            return
        }
        val q = query.trim().lowercase()
        _uiState.update {
            it.copy(
                resultadosAlbumes  = todosLosAlbumes.filter { a ->
                    a.nombre.lowercase().contains(q) || a.artista.lowercase().contains(q)
                },
                resultadosArtistas = todosLosArtistas.filter { a ->
                    a.nombre.lowercase().contains(q)
                }
            )
        }
    }

    fun limpiar() {
        _uiState.update { BuscarUIState() }
    }
}