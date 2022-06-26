package ru.netology.inmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.inmedia.dto.*

interface UserRepository {

    val data: Flow<List<User>>

    val wall: Flow<List<Post>>

    suspend fun getAllUsers()

    suspend fun getUserById(id: Long): User

    suspend fun upload(upload: MediaUpload): Media

    suspend fun saveUserAvatar(user: User, upload: MediaUpload)
}