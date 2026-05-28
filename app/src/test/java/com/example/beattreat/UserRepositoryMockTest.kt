package com.example.beattreat

import com.example.beattreat.data.datasource.implementation.UserRetrofitDataSourceImplementation
import com.example.beattreat.data.dto.UserDto
import com.example.beattreat.data.repository.UserRepository
import com.google.common.truth.Truth
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody


class UserRepositoryMockTest {

    private lateinit var dataSource: UserRetrofitDataSourceImplementation
    private lateinit var userRepository: UserRepository

    private fun fakeUserDto(id: Int) = UserDto(
        id           = id,
        username     = "username_$id",
        name         = "Name $id",
        bio          = "Bio $id",
        profileImage = "https://picsum.photos/200/200?random=$id",
        email        = "user$id@example.com"
    )

    @Before
    fun setUp() {
        dataSource     = mockk()
        userRepository = UserRepository(dataSource)
    }

    // ─── TEST 1: getUserById éxito → nombre mapeado correctamente ────────────
    @Test
    fun getUserById_success_nameMappedCorrectly() = runTest {
        // arrange
        coEvery { dataSource.getUserById(1) } returns fakeUserDto(1)
        // act
        val result = userRepository.getUserById(1)
        // assert
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()?.nombre).isEqualTo("Name 1")
    }

    // ─── TEST 2: getUserById → username con @ ─────────────────────────────────
    @Test
    fun getUserById_success_usernameHasAtSign() = runTest {
        // arrange
        coEvery { dataSource.getUserById(2) } returns fakeUserDto(2)
        // act
        val result = userRepository.getUserById(2)
        // assert
        Truth.assertThat(result.getOrNull()?.username).isEqualTo("@username_2")
    }

    // ─── TEST 3: getUserById → id mapeado correctamente ───────────────────────
    @Test
    fun getUserById_success_idMappedCorrectly() = runTest {
        // arrange
        coEvery { dataSource.getUserById(5) } returns fakeUserDto(5)
        // act
        val result = userRepository.getUserById(5)
        // assert
        Truth.assertThat(result.getOrNull()?.id).isEqualTo(5)
    }

    // ─── TEST 4: getUserById → bio mapeada correctamente ─────────────────────
    @Test
    fun getUserById_success_bioMappedCorrectly() = runTest {
        // arrange
        coEvery { dataSource.getUserById(3) } returns fakeUserDto(3)
        // act
        val result = userRepository.getUserById(3)
        // assert
        Truth.assertThat(result.getOrNull()?.bio).isEqualTo("Bio 3")
    }

    // ─── TEST 5: getUserById HTTP 404 → Result.failure ────────────────────────
    @Test
    fun getUserById_http404_returnsFailure() = runTest {
        // arrange
        coEvery { dataSource.getUserById(99) } throws retrofit2.HttpException(
            retrofit2.Response.error<Any>(404, okhttp3.ResponseBody.create(null, ""))
        )
        // act
        val result = userRepository.getUserById(99)
        // assert
        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()?.message).contains("no encontrado")
    }

    // ─── TEST 6: getUserById sin internet → mensaje amigable ──────────────────
    @Test
    fun getUserById_noInternet_returnsConnectionError() = runTest {
        // arrange
        coEvery { dataSource.getUserById(any()) } throws java.net.UnknownHostException()
        // act
        val result = userRepository.getUserById(1)
        // assert
        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()?.message).contains("internet")
    }

    // ─── TEST 7: getUserById timeout → mensaje amigable ───────────────────────
    @Test
    fun getUserById_timeout_returnsTimeoutError() = runTest {
        // arrange
        coEvery { dataSource.getUserById(any()) } throws java.net.SocketTimeoutException()
        // act
        val result = userRepository.getUserById(1)
        // assert
        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()?.message).contains("tardó")
    }

    // ─── TEST 8: getUserById profileImage null → string vacío ────────────────
    @Test
    fun getUserById_nullProfileImage_mapsToEmptyString() = runTest {
        // arrange
        coEvery { dataSource.getUserById(7) } returns UserDto(
            id           = 7,
            username     = "username_7",
            name         = "Name 7",
            bio          = null,
            profileImage = null,
            email        = "user7@example.com"
        )
        // act
        val result = userRepository.getUserById(7)
        // assert
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()?.fotoPerfilUrl).isEqualTo("")
    }
}
