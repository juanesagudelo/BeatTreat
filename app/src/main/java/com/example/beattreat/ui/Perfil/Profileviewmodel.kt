package com.example.beattreat.ui.Perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.AuthRepository
import com.example.beattreat.data.repository.FollowRepository
import com.example.beattreat.data.repository.FirestoreAlbumRepository
import com.example.beattreat.data.repository.FirestoreReviewRepository
import com.example.beattreat.data.repository.FirestoreUserRepository
import com.example.beattreat.data.repository.StorageRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestoreUserRepository: FirestoreUserRepository,
    private val firestoreReviewRepository: FirestoreReviewRepository,
    private val firestoreAlbumRepository: FirestoreAlbumRepository,
    private val followRepository: FollowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    init {
        cargarPerfil()
        cargarResenasFirestore()
    }

    fun refresh() {
        cargarPerfil()
        cargarResenasFirestore()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            val perfilDeferred    = async { firestoreUserRepository.getMyProfile() }
            val followersDeferred = async {
                if (currentUserId.isNotBlank()) followRepository.getFollowersCount(currentUserId).getOrNull() ?: 0 else 0
            }
            val followingDeferred = async {
                if (currentUserId.isNotBlank()) followRepository.getFollowingCount(currentUserId).getOrNull() ?: 0 else 0
            }
            val favoritosDeferred = async { firestoreUserRepository.getFavorites() }

            val perfilResult   = perfilDeferred.await()
            val followersCount = followersDeferred.await()
            val followingCount = followingDeferred.await()
            val favoritosResult = favoritosDeferred.await()

            val perfil = perfilResult.getOrElse {
                val urlDeFirebaseAuth = firebaseAuth.currentUser?.photoUrl?.toString() ?: ""
                PerfilData.perfilActual.copy(fotoPerfilUrl = urlDeFirebaseAuth)
            }

            val perfilConContadores = perfil.copy(seguidores = followersCount, siguiendo = followingCount)
            PerfilData.perfilActual = perfilConContadores

            val albumesFavoritos = favoritosResult.getOrDefault(emptyList()).take(6).map { favMap ->
                val firestoreId = favMap["firestoreId"] ?: ""
                AlbumPerfilUI(
                    id        = firestoreId.hashCode(),
                    nombre    = favMap["title"] ?: "",
                    imagenUrl = favMap["coverImage"] ?: ""
                )
            }

            _uiState.update {
                it.copy(
                    perfil           = PerfilData.perfilActual,
                    albumesFavoritos = albumesFavoritos,
                    isLoading        = false
                )
            }
        }
    }

    private fun cargarResenasFirestore() {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            val reviewsDeferred = async { firestoreReviewRepository.getReviewsByUser(currentUserId) }
            val albumsDeferred  = async { firestoreAlbumRepository.getAllAlbumsRaw() }
            val reviewsResult   = reviewsDeferred.await()
            val albumsResult    = albumsDeferred.await()
            val albumsMap       = albumsResult.getOrDefault(emptyMap())

            reviewsResult.onSuccess { resenas ->
                val resenasUI = resenas.take(6).map { resena ->
                    val albumDto = albumsMap[resena.albumId.toString()]
                        ?: albumsMap.entries.find { it.key.hashCode().toString() == resena.albumId.toString() }?.value
                    ResenaConAlbumUI(
                        id           = resena.id.hashCode(),
                        autorNombre  = PerfilData.perfilActual.nombre,
                        autorUsuario = PerfilData.perfilActual.usuario,
                        autorFotoUrl = PerfilData.perfilActual.fotoPerfilUrl,
                        texto        = resena.texto,
                        likes        = 0,
                        comentarios  = 0,
                        albumNombre  = albumDto?.title  ?: resena.albumNombre,
                        albumArtista = albumDto?.artist ?: resena.albumArtista,
                        albumCover   = albumDto?.coverImage ?: resena.albumImagenUrl,
                        calificacion = resena.calificacion
                    )
                }
                _uiState.update { it.copy(resenasConAlbum = resenasUI) }
            }
        }
    }

    fun refrescarPerfil() {
        cargarPerfil()
        cargarResenasFirestore()
    }

    fun refrescarFotoPerfil() {
        val urlFirebaseAuth = firebaseAuth.currentUser?.photoUrl?.toString() ?: ""
        val urlFinal = urlFirebaseAuth.ifBlank { PerfilData.perfilActual.fotoPerfilUrl }
        _uiState.update { state -> state.copy(perfil = state.perfil?.copy(fotoPerfilUrl = urlFinal)) }
    }

    fun cerrarSesion() {
        PerfilData.perfilActual = PerfilData.perfilActual.copy(fotoPerfilUrl = "")
        authRepository.signOut()
        _uiState.update { it.copy(cerrarSesionExitoso = true) }
    }

    fun resetCerrarSesion() {
        _uiState.update { it.copy(cerrarSesionExitoso = false) }
    }
}