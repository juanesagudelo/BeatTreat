package com.example.beattreat.data.repository

import com.example.beattreat.data.datasource.FirestoreReviewRemoteDataSource
import com.example.beattreat.ui.MapaResenas.ResenaMapaUI
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MapaResenasRepository @Inject constructor(
    private val reviewDataSource:         FirestoreReviewRemoteDataSource,
    private val firestoreAlbumRepository: FirestoreAlbumRepository
) {

    suspend fun getResenasUltimas24Horas(): Result<List<ResenaMapaUI>> {
        return try {
            coroutineScope {
                val resenasDeferred = async { reviewDataSource.getReviewsUltimas24Horas() }
                val albumsDeferred  = async { firestoreAlbumRepository.getAllAlbumsRaw().getOrDefault(emptyMap()) }

                val pairs     = resenasDeferred.await()
                val albumsMap = albumsDeferred.await()

                val resenas = pairs.map { (docId, dto) ->
                    val albumDto = albumsMap[dto.albumId]
                    ResenaMapaUI(
                        firestoreDocId = docId,
                        albumId        = dto.albumId,   // ← incluido
                        autorNombre    = dto.user.name.ifBlank { "Usuario" },
                        autorUsuario   = "@${dto.user.username}",
                        autorFotoUrl   = dto.user.profileImage ?: "",
                        albumNombre    = albumDto?.title ?: "Álbum desconocido",
                        calificacion   = dto.rating,
                        texto          = dto.content,
                        fecha          = formatTimestamp(dto.createdAt),
                        latitud        = dto.latitud,
                        longitud       = dto.longitud
                    )
                }
                Result.success(resenas)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al cargar reseñas del mapa: ${e.message}"))
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        return try {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
        } catch (e: Exception) { "" }
    }
}