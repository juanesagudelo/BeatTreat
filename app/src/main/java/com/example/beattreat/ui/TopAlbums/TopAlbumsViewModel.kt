package com.example.beattreat.ui.TopAlbums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.FirestoreAlbumRepository
import com.example.beattreat.data.repository.FirestoreReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class TopAlbumsViewModel @Inject constructor(
    private val albumRepository:  FirestoreAlbumRepository,
    private val reviewRepository: FirestoreReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopAlbumsUIState())
    val uiState: StateFlow<TopAlbumsUIState> = _uiState.asStateFlow()

    private var todosLosAlbums: List<TopAlbumUI> = emptyList()

    val generos = listOf(
        "Todos", "Reggaetón", "Popular Colombiana", "Corridos Mexicanos",
        "Vallenato", "Trap Latino", "Urbano", "Cumbia", "Latin Pop"
    )

    init {
        cargarTopAlbums()
    }

    fun cargarTopAlbums() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val albumsResult = albumRepository.getAllAlbumsRaw()

            if (albumsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = "No se pudieron cargar los álbumes"
                    )
                }
                return@launch
            }

            val albumsMap = albumsResult.getOrDefault(emptyMap())

            val topAlbums = albumsMap.entries.map { (firestoreId, dto) ->
                val reviewsResult = reviewRepository.getReviewsByAlbum(firestoreId)
                val reviews       = reviewsResult.getOrDefault(emptyList())
                val rating        = if (reviews.isNotEmpty())
                    (round(reviews.map { it.calificacion }.average() * 10) / 10).toFloat()
                else 0f

                TopAlbumUI(
                    firestoreId  = firestoreId,
                    titulo       = dto.title,
                    artista      = dto.artist,
                    genero       = dto.genre,
                    coverImage   = dto.coverImage,
                    rating       = rating,
                    totalResenas = reviews.size
                )
            }.sortedByDescending { it.rating }

            todosLosAlbums = topAlbums
            _uiState.update { it.copy(albums = topAlbums, isLoading = false) }
        }
    }

    fun filtrarPorGenero(genero: String) {
        _uiState.update { state ->
            val filtrados = if (genero == "Todos") todosLosAlbums
                           else todosLosAlbums.filter { it.genero == genero }
            state.copy(generoSeleccionado = genero, albums = filtrados)
        }
    }
}
