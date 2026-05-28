package com.example.beattreat.data.repository

import com.example.beattreat.data.datasource.FirestoreComentarioRemoteDataSource
import com.example.beattreat.data.dto.FirestoreComentarioDto
import com.example.beattreat.data.dto.FirestoreReviewUserDto
import com.example.beattreat.ui.Comentarios.ComentarioFirestoreUI
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FirestoreComentarioRepository @Inject constructor(
    private val dataSource:    FirestoreComentarioRemoteDataSource,
    private val firebaseAuth:  FirebaseAuth,
    private val userRepository: FirestoreUserRepository
) {

    suspend fun getComentarios(reviewId: String): Result<List<ComentarioFirestoreUI>> {
        return try {
            val pairs = dataSource.getComentariosByReview(reviewId)
            val comentarios = pairs.map { (docId, dto) ->
                ComentarioFirestoreUI(
                    id           = docId,
                    autorNombre  = dto.user.name.ifBlank { "Usuario" },
                    autorUsuario = "@${dto.user.username}",
                    autorFotoUrl = dto.user.profileImage ?: "",
                    texto        = dto.content,
                    likes        = dto.likesCount,
                    fecha        = formatTimestamp(dto.createdAt)
                )
            }.sortedBy { it.fecha }
            Result.success(comentarios)
        } catch (e: Exception) {
            Result.failure(Exception("Error al cargar comentarios: ${e.message}"))
        }
    }

    suspend fun createComentario(reviewId: String, content: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: throw Exception("Debes iniciar sesión para comentar")
            val userId  = currentUser.uid
            val userDto = userRepository.getUserById(userId).getOrNull()
            val dto = FirestoreComentarioDto(
                reviewId  = reviewId,
                userId    = userId,
                content   = content,
                createdAt = System.currentTimeMillis(),
                user      = FirestoreReviewUserDto(
                    name         = userDto?.name?.takeIf { it.isNotBlank() } ?: currentUser.displayName ?: "Usuario",
                    username     = userDto?.username?.takeIf { it.isNotBlank() } ?: "",
                    profileImage = userDto?.profileImage ?: currentUser.photoUrl?.toString()
                )
            )
            dataSource.createComentario(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear comentario: ${e.message}"))
        }
    }

    suspend fun toggleLike(comentarioId: String): Result<Int> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: throw Exception("Debes iniciar sesión para dar like")
            val nuevosLikes = dataSource.toggleLike(comentarioId, userId)
            Result.success(nuevosLikes)
        } catch (e: Exception) {
            Result.failure(Exception("Error al dar like: ${e.message}"))
        }
    }

    private fun formatTimestamp(ts: Long): String {
        if (ts == 0L) return ""
        return try {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ts))
        } catch (e: Exception) { "" }
    }
}