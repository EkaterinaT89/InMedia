package ru.netology.inmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.dto.User

interface WallRepository {

    val data: Flow<List<Post>>

    suspend fun getUserWall(id: Long)

    suspend fun likePostsOnWall(authorId: Long, postId: Long)

}