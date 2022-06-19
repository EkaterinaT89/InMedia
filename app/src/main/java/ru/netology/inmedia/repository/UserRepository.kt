package ru.netology.inmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.inmedia.dto.Event
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.dto.User

interface UserRepository {

    val data: Flow<List<User>>

    val wall: Flow<List<Post>>

    suspend fun getAllUsers()

    suspend fun getUserById(id: Long)



}