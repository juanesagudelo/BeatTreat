package com.example.beattreat

import android.util.Log
import com.example.beattreat.data.datasource.implementation.firestore.UserFirestoreDataSourceImpl
import com.example.beattreat.data.dto.FirestoreUserDto
import com.example.beattreat.data.dto.RegisterUserDto
import com.example.beattreat.data.repository.FirestoreUserRepository
import com.google.common.truth.Truth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirestoreUserRepositoryIntegrationTest {

    private val db   = Firebase.firestore
    private val auth = Firebase.auth

    private lateinit var userRepository: FirestoreUserRepository
    private lateinit var dataSource: UserFirestoreDataSourceImpl

    private fun generateUser(i: Int): RegisterUserDto = RegisterUserDto(
        username = "username_$i",
        name     = "Name $i",
        country  = "Colombia",
        bio      = "Bio de usuario $i"
    )

    @Before
    fun setUp() = runTest {
        try {
            db.useEmulator("10.0.2.2", 8080)
        } catch (e: Exception) { }

        dataSource     = UserFirestoreDataSourceImpl(db)
        userRepository = FirestoreUserRepository(dataSource, auth)

        // replicables
        // autonomas

        val batch = db.batch()
        repeat(times = 10) { i ->
            val user    = generateUser(i)
            val userRef = db.collection("users").document("user_$i")
            batch.set(userRef, user)
        }
        Log.d("TAG", "Antes del batch")
        batch.commit().await()
        Log.d("TAG", "Después del batch")
    }

    // ─── TEST 1: getUserById ID válido -> Result.success ───────────────────────
    @Test
    fun getUserById_validId_correctUser() = runTest {
        //arrange
        val id           = "user_9"
        val expectedName = "Name 9"
        //act
        val result = userRepository.getUserById(id)
        //assert
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()?.name).isEqualTo(expectedName)
    }

    // ─── TEST 2: getUserById ID inválido -> Result.failure ─────────────────────
    @Test
    fun getUserById_invalidId_returnFailure() = runTest {
        //arrange
        val id = "user_999"
        //act
        val result = userRepository.getUserById(id)
        //assert
        Truth.assertThat(result.isFailure).isTrue()
    }

    // ─── TEST 3: getUserById -> username correcto ──────────────────────────────
    @Test
    fun getUserById_validId_correctUsername() = runTest {
        //arrange
        val id               = "user_3"
        val expectedUsername = "username_3"
        //act
        val result = userRepository.getUserById(id)
        //assert
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()?.username).isEqualTo(expectedUsername)
    }

    // ─── TEST 4: getUserById -> country correcto ───────────────────────────────
    @Test
    fun getUserById_validId_correctCountry() = runTest {
        //arrange
        val id              = "user_7"
        val expectedCountry = "Colombia"
        //act
        val result = userRepository.getUserById(id)
        //assert
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()?.country).isEqualTo(expectedCountry)
    }

    // ─── TEST 5: addFavorite via dataSource -> isFavorite true ─────────────────
    @Test
    fun addFavorite_viaDataSource_isFavoriteTrue() = runTest {
        //arrange
        val userId  = "user_1"
        val albumId = "album_fav_test"
        //act
        dataSource.addFavorite(userId, albumId, mapOf(
            "title"      to "Un Verano Sin Ti",
            "artist"     to "Bad Bunny",
            "coverImage" to "https://picsum.photos/300/300",
            "addedAt"    to System.currentTimeMillis()
        ))
        //assert
        Truth.assertThat(dataSource.isFavorite(userId, albumId)).isTrue()
    }

    // ─── TEST 6: removeFavorite via dataSource -> isFavorite false ─────────────
    @Test
    fun removeFavorite_viaDataSource_isFavoriteFalse() = runTest {
        //arrange
        val userId  = "user_2"
        val albumId = "album_to_remove"
        dataSource.addFavorite(userId, albumId, mapOf(
            "title"      to "Génesis",
            "artist"     to "Peso Pluma",
            "coverImage" to "https://picsum.photos/300/300",
            "addedAt"    to System.currentTimeMillis()
        ))
        //act
        dataSource.removeFavorite(userId, albumId)
        //assert
        Truth.assertThat(dataSource.isFavorite(userId, albumId)).isFalse()
    }

    // ─── TEST 7: updateUser via dataSource -> campos actualizados ──────────────
    @Test
    fun updateUser_viaDataSource_fieldsUpdated() = runTest {
        //arrange
        val userId = "user_5"
        val dto    = FirestoreUserDto(
            username = "username_nuevo",
            name     = "Nombre Nuevo",
            country  = "Argentina",
            bio      = "Bio nueva"
        )
        //act
        dataSource.updateUser(userId, dto)
        //assert
        val result = userRepository.getUserById(userId)
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()?.name).isEqualTo("Nombre Nuevo")
        Truth.assertThat(result.getOrNull()?.country).isEqualTo("Argentina")
    }

    // ─── TEST 8: getUserById múltiples usuarios -> todos correctos ────────────
    @Test
    fun getUserById_multipleUsers_allCorrect() = runTest {
        //arrange & act & assert
        for (i in 1..5) {
            val result = userRepository.getUserById("user_$i")
            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.getOrNull()?.name).isEqualTo("Name $i")
        }
    }

    @After
    fun tearDown() = runTest {
        val users = db.collection("users").get().await()
        users.documents.forEach { doc ->
            db.collection("users").document(doc.id).delete().await()
        }
    }
}