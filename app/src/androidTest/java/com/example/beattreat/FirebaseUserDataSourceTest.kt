package com.example.beattreat

import android.util.Log
import com.example.beattreat.data.datasource.implementation.firestore.UserFirestoreDataSourceImpl
import com.example.beattreat.data.dto.FirestoreUserDto
import com.example.beattreat.data.dto.RegisterUserDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.common.truth.Truth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirebaseUserDataSourceTest {

    private val db = Firebase.firestore

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

        dataSource = UserFirestoreDataSourceImpl(db)

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

    // ─── TEST 1: Obtener usuario por ID válido ────────────────────────────────
    @Test
    fun getUserById_validId_correctUser() = runTest {
        //AAA
        //Arrange
        val id           = "user_9"
        val expectedName = "Name 9"
        //Act
        val result = dataSource.getUserById(id)
        //Assert
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result?.name).isEqualTo(expectedName)
        Truth.assertThat(result?.username).isEqualTo("username_9")
    }

    // ─── TEST 2: Obtener usuario con ID inválido -> null ───────────────────────
    @Test
    fun getUserById_invalidId_null() = runTest {
        //AAA
        //Arrange
        val id = "user_999"
        //Act
        val result = dataSource.getUserById(id)
        //Assert
        Truth.assertThat(result).isNull()
    }

    // ─── TEST 3: Registrar usuario nuevo ─────────────────────────────────────
    @Test
    fun registerUser_newUser_userExistsInFirestore() = runTest {
        //AAA
        //Arrange
        val newUserId = "user_new_test"
        val dto = RegisterUserDto(
            username = "nuevo_usuario",
            name     = "Usuario Nuevo",
            country  = "México",
            bio      = "Soy nuevo"
        )
        //Act
        dataSource.registerUser(newUserId, dto)
        //Assert
        val snapshot = db.collection("users").document(newUserId).get().await()
        Truth.assertThat(snapshot.exists()).isTrue()
        Truth.assertThat(snapshot.getString("username")).isEqualTo("nuevo_usuario")
    }

    // ─── TEST 4: Actualizar usuario ───────────────────────────────────────────
    @Test
    fun updateUser_existingUser_fieldsAreUpdated() = runTest {
        //AAA
        //Arrange
        val userId     = "user_5"
        val updatedDto = FirestoreUserDto(
            username = "username_actualizado",
            name     = "Nombre Actualizado",
            country  = "Argentina",
            bio      = "Bio actualizada"
        )
        //Act
        dataSource.updateUser(userId, updatedDto)
        //Assert
        val result = dataSource.getUserById(userId)
        Truth.assertThat(result?.name).isEqualTo("Nombre Actualizado")
        Truth.assertThat(result?.username).isEqualTo("username_actualizado")
        Truth.assertThat(result?.country).isEqualTo("Argentina")
    }

    // ─── TEST 5: Agregar favorito ─────────────────────────────────────────────
    @Test
    fun addFavorite_newAlbum_isFavoriteTrue() = runTest {
        //AAA
        //Arrange
        val userId  = "user_1"
        val albumId = "album_test_1"
        //Act
        dataSource.addFavorite(userId, albumId, mapOf(
            "title"      to "Un Verano Sin Ti",
            "artist"     to "Bad Bunny",
            "coverImage" to "https://picsum.photos/300/300",
            "addedAt"    to System.currentTimeMillis()
        ))
        //Assert
        Truth.assertThat(dataSource.isFavorite(userId, albumId)).isTrue()
    }

    // ─── TEST 6: isFavorite -> false cuando no existe ──────────────────────────
    @Test
    fun isFavorite_albumNotAdded_returnsFalse() = runTest {
        //AAA
        //Arrange
        val userId  = "user_2"
        val albumId = "album_que_no_existe_999"
        //Act
        val result = dataSource.isFavorite(userId, albumId)
        //Assert
        Truth.assertThat(result).isFalse()
    }

    // ─── TEST 7: Eliminar favorito ────────────────────────────────────────────
    @Test
    fun removeFavorite_existingFavorite_isFavoriteFalse() = runTest {
        //AAA
        //Arrange
        val userId  = "user_3"
        val albumId = "album_to_remove"
        dataSource.addFavorite(userId, albumId, mapOf(
            "title"      to "Génesis",
            "artist"     to "Peso Pluma",
            "coverImage" to "https://picsum.photos/300/300",
            "addedAt"    to System.currentTimeMillis()
        ))
        //Act
        dataSource.removeFavorite(userId, albumId)
        //Assert
        Truth.assertThat(dataSource.isFavorite(userId, albumId)).isFalse()
    }

    // ─── TEST 8: getFavorites -> lista correcta ────────────────────────────────
    @Test
    fun getFavorites_userWithFavorites_returnsCorrectList() = runTest {
        //AAA
        //Arrange
        val userId = "user_4"
        listOf(
            Triple("album_1", "Reggaetón Mix",  "Bad Bunny"),
            Triple("album_2", "Clandestino",    "Manu Chao"),
            Triple("album_3", "Amor Prohibido", "Selena")
        ).forEach { (albumId, title, artist) ->
            dataSource.addFavorite(userId, albumId, mapOf(
                "title"      to title,
                "artist"     to artist,
                "coverImage" to "https://picsum.photos/300/300",
                "addedAt"    to System.currentTimeMillis()
            ))
        }
        //Act
        val favorites = dataSource.getFavorites(userId)
        //Assert
        Truth.assertThat(favorites).isNotEmpty()
        Truth.assertThat(favorites.size).isEqualTo(3)
    }

    @After
    fun tearDown() = runTest {
        val users = db.collection("users").get().await()
        users.documents.forEach { doc ->
            db.collection("users").document(doc.id).delete().await()
        }
    }
}