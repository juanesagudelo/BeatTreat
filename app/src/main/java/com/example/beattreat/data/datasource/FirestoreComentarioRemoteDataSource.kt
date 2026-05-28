package com.example.beattreat.data.datasource

import com.example.beattreat.data.dto.FirestoreComentarioDto

interface FirestoreComentarioRemoteDataSource {
    suspend fun getComentariosByReview(reviewId: String): List<Pair<String, FirestoreComentarioDto>>
    suspend fun createComentario(dto: FirestoreComentarioDto): String
    suspend fun toggleLike(comentarioId: String, userId: String): Int
}