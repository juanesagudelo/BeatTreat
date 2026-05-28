package com.example.beattreat.data.datasource.implementation.firestore

import com.example.beattreat.data.datasource.FirestoreComentarioRemoteDataSource
import com.example.beattreat.data.dto.FirestoreComentarioDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ComentarioFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : FirestoreComentarioRemoteDataSource {

    companion object {
        private const val COMENTARIOS_COLLECTION = "comentarios"
        private const val LIKES_COLLECTION        = "likes"
    }

    override suspend fun getComentariosByReview(reviewId: String): List<Pair<String, FirestoreComentarioDto>> {
        val snapshot = db.collection(COMENTARIOS_COLLECTION)
            .whereEqualTo("reviewId", reviewId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val dto = doc.toObject(FirestoreComentarioDto::class.java) ?: return@mapNotNull null
            doc.id to dto
        }
    }

    override suspend fun createComentario(dto: FirestoreComentarioDto): String {
        val ref = db.collection(COMENTARIOS_COLLECTION).add(dto).await()
        return ref.id
    }

    override suspend fun toggleLike(comentarioId: String, userId: String): Int {
        val likeRef = db.collection(COMENTARIOS_COLLECTION)
            .document(comentarioId)
            .collection(LIKES_COLLECTION)
            .document(userId)

        val likeDoc  = likeRef.get().await()
        val comentRef = db.collection(COMENTARIOS_COLLECTION).document(comentarioId)

        return if (likeDoc.exists()) {
            likeRef.delete().await()
            val snap = comentRef.get().await()
            val actual = (snap.getLong("likesCount") ?: 1L) - 1L
            comentRef.update("likesCount", actual).await()
            actual.toInt()
        } else {
            likeRef.set(mapOf("userId" to userId, "createdAt" to System.currentTimeMillis())).await()
            val snap = comentRef.get().await()
            val actual = (snap.getLong("likesCount") ?: 0L) + 1L
            comentRef.update("likesCount", actual).await()
            actual.toInt()
        }
    }
}