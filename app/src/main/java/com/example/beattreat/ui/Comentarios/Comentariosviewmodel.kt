package com.example.beattreat.ui.Comentarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.FirestoreComentarioRepository
import com.example.beattreat.data.repository.FirestoreReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComentariosViewModel @Inject constructor(
    private val comentarioRepository: FirestoreComentarioRepository,
    private val reviewRepository:     FirestoreReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComentariosUIState())
    val uiState: StateFlow<ComentariosUIState> = _uiState.asStateFlow()

    private var currentResenaId: String = ""

    fun cargarComentarios(resenaId: String, albumId: String) {
        currentResenaId = resenaId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            // Cargar reseña y comentarios en paralelo
            val resenaDeferred      = async { reviewRepository.getReviewsByAlbum(albumId) }
            val comentariosDeferred = async { comentarioRepository.getComentarios(resenaId) }

            val resenaResult      = resenaDeferred.await()
            val comentariosResult = comentariosDeferred.await()

            val resena = resenaResult.getOrDefault(emptyList())
                .find { it.id == resenaId }

            _uiState.update {
                it.copy(
                    resena       = resena,
                    comentarios  = comentariosResult.getOrDefault(emptyList()),
                    isLoading    = false,
                    errorMessage = comentariosResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun onNuevoComentarioChange(texto: String) {
        _uiState.update { it.copy(nuevoComentario = texto) }
    }

    fun enviarComentario() {
        val texto = _uiState.value.nuevoComentario.trim()
        if (texto.isBlank() || currentResenaId.isBlank()) return

        _uiState.update { it.copy(enviando = true) }

        viewModelScope.launch {
            val result = comentarioRepository.createComentario(currentResenaId, texto)
            if (result.isSuccess) {
                // Limpiar campo y recargar comentarios
                _uiState.update { it.copy(nuevoComentario = "", enviando = false) }
                val nuevos = comentarioRepository.getComentarios(currentResenaId)
                _uiState.update { it.copy(comentarios = nuevos.getOrDefault(emptyList())) }
            } else {
                _uiState.update {
                    it.copy(
                        enviando     = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun toggleLikeComentario(comentarioId: String) {
        viewModelScope.launch {
            val result = comentarioRepository.toggleLike(comentarioId)
            if (result.isSuccess) {
                val nuevosLikes = result.getOrDefault(0)
                _uiState.update { state ->
                    val likeados = state.comentariosLikeados
                    val nuevosLikeados = if (comentarioId in likeados)
                        likeados - comentarioId
                    else
                        likeados + comentarioId
                    state.copy(
                        comentariosLikeados = nuevosLikeados,
                        comentarios = state.comentarios.map { c ->
                            if (c.id == comentarioId) c.copy(likes = nuevosLikes) else c
                        }
                    )
                }
            }
        }
    }
}