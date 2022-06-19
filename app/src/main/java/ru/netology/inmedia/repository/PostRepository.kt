package ru.netology.inmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.inmedia.dto.Media
import ru.netology.inmedia.dto.MediaUpload
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.AttachmentType

interface PostRepository {

    val data: Flow<List<Post>>

    suspend fun save(post: Post)

    suspend fun getAll()

    suspend fun getLastTen()

    fun getNewerCount(id: Long): Flow<Int>

    suspend fun getById(id: Long): Post

    suspend fun edit(post: Post)

    suspend fun likeById(id: Long)

    suspend fun disLikeById(id: Long)

    suspend fun removeById(id: Long)

    suspend fun getPostNotExist(id: Long)

    suspend fun saveWithAttachment(post: Post, upload: MediaUpload, type: AttachmentType)

    suspend fun upload(upload: MediaUpload): Media

}