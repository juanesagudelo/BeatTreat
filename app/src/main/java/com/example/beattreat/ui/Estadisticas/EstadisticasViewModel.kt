package com.example.beattreat.ui.Estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.FirestoreAlbumRepository
import com.example.beattreat.data.repository.FirestoreReviewRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val reviewRepository: FirestoreReviewRepository,
    private val albumRepository:  FirestoreAlbumRepository,
    private val firebaseAuth:     FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUIState())
    val uiState: StateFlow<EstadisticasUIState> = _uiState.asStateFlow()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val reviewsDeferred = async { reviewRepository.getReviewsByUserRaw(userId) }
            val albumsDeferred  = async { albumRepository.getAllAlbumsRaw() }

            val reviewsResult = reviewsDeferred.await()
            val albumsMap     = albumsDeferred.await().getOrDefault(emptyMap())

            reviewsResult.onSuccess { pairs ->
                if (pairs.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@onSuccess
                }

                val ratings = pairs.map { it.second.rating }

                // Rating promedio
                val promedio = (round(ratings.average() * 10) / 10).toFloat()

                // Géneros favoritos
                val generosCont = pairs
                    .mapNotNull { (_, dto) -> albumsMap[dto.albumId]?.genre }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .take(4)

                val totalGeneros = generosCont.sumOf { it.value }.toFloat()
                val generosFav = generosCont.map { (nombre, cantidad) ->
                    GeneroEstadisticaUI(
                        nombre      = nombre,
                        cantidad    = cantidad,
                        porcentaje  = if (totalGeneros > 0) (cantidad / totalGeneros) * 100f else 0f
                    )
                }

                // Álbum más reciente
                val masReciente = pairs
                    .maxByOrNull { it.second.createdAt }
                    ?.let { (_, dto) -> albumsMap[dto.albumId]?.title ?: "Desconocido" }
                    ?: ""

                // Racha: días consecutivos con al menos una reseña
                val racha = calcularRacha(pairs.map { it.second.createdAt })

                _uiState.update {
                    it.copy(
                        totalResenas     = pairs.size,
                        ratingPromedio   = promedio,
                        ratingMaximo     = ratings.max(),
                        ratingMinimo     = ratings.min(),
                        generosFavoritos = generosFav,
                        albumMasReciente = masReciente,
                        rachaActual      = racha,
                        isLoading        = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun calcularRacha(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val hoy = Calendar.getInstance()
        val diasConResena = timestamps.map { ts ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = ts
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }.toSet()

        var racha = 0
        val cal = Calendar.getInstance()

        while (true) {
            val key = Triple(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            if (diasConResena.contains(key)) {
                racha++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            } else break
        }

        return racha
    }
}
