package com.example.beattreat.data.datasource

import com.example.beattreat.data.dto.FirestoreUserDto
import com.example.beattreat.data.dto.RegisterUserDto

interface FirestoreUserRemoteDataSource {
    suspend fun registerUser(userId: String, dto: RegisterUserDto)
    suspend fun getUserById(userId: String): FirestoreUserDto
    suspend fun updateUser(userId: String, dto: FirestoreUserDto)
    suspend fun addFavorite(userId: String, albumId: String, data: Map<String, Any>)
    suspend fun removeFavorite(userId: String, albumId: String)
    suspend fun isFavorite(userId: String, albumId: String): Boolean
    suspend fun getFavorites(userId: String): List<Map<String, String>>
}