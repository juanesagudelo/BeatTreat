package com.example.beattreat.data.datasource.implementation.firestore

import com.example.beattreat.data.datasource.FirestoreUserRemoteDataSource
import com.example.beattreat.data.dto.FirestoreUserDto
import com.example.beattreat.data.dto.RegisterUserDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : FirestoreUserRemoteDataSource {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val FAVORITES_COL = "favorites"
    }

    override suspend fun registerUser(userId: String, dto: RegisterUserDto) {
        db.collection(USERS_COLLECTION).document(userId).set(dto).await()
    }

    override suspend fun getUserById(userId: String): FirestoreUserDto {
        val snapshot = db.collection(USERS_COLLECTION).document(userId).get().await()
        return snapshot.toObject(FirestoreUserDto::class.java)
            ?: throw Exception("No se pudo obtener los datos del usuario")
    }

    override suspend fun updateUser(userId: String, dto: FirestoreUserDto) {
        val updates = mutableMapOf<String, Any>()
        if (dto.username.isNotBlank()) updates["username"] = dto.username
        if (dto.name.isNotBlank()) updates["name"] = dto.name
        dto.bio?.let { updates["bio"] = it }
        dto.profileImage?.let { if (it.isNotBlank()) updates["profileImage"] = it }
        dto.country?.let { if (it.isNotBlank()) updates["country"] = it }
        if (updates.isNotEmpty()) {
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
        }
    }

    override suspend fun addFavorite(userId: String, albumId: String, data: Map<String, Any>) {
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FAVORITES_COL)
            .document(albumId)
            .set(data)
            .await()
    }

    override suspend fun removeFavorite(userId: String, albumId: String) {
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FAVORITES_COL)
            .document(albumId)
            .delete()
            .await()
    }

    override suspend fun isFavorite(userId: String, albumId: String): Boolean {
        val doc = db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FAVORITES_COL)
            .document(albumId)
            .get()
            .await()
        return doc.exists()
    }

    override suspend fun getFavorites(userId: String): List<Map<String, String>> {
        val snapshot = db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(FAVORITES_COL)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val artist = doc.getString("artist") ?: return@mapNotNull null
            val coverImage = doc.getString("coverImage") ?: ""
            mapOf(
                "firestoreId" to doc.id,
                "title" to title,
                "artist" to artist,
                "coverImage" to coverImage
            )
        }
    }
}