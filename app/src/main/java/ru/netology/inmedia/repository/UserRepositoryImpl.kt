package ru.netology.inmedia.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.inmedia.api.ApiService
import ru.netology.inmedia.dto.*
import ru.netology.inmedia.error.ApiException
import ru.netology.inmedia.error.AppError
import ru.netology.inmedia.error.NetWorkException
import ru.netology.inmedia.error.UnknownException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    val listData = mutableListOf<User>()

    private val _users = MutableLiveData<List<User>>()

    override val data = _users.asFlow().flowOn(Dispatchers.Default)

    private val _wall = MutableLiveData<List<Post>>()

    override val wall = _wall.asFlow().flowOn(Dispatchers.Default)

    override suspend fun getAllUsers() {
        try {
            val response = apiService.getAllUsers()
            val users = response.body() ?: throw ApiException(response.code(), response.message())
            for (user in users) {
                if (!listData.contains(user)) {
                    listData.add(user)
                }
            }
            _users.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun getUserById(id: Long): User {
        try {
            val response = apiService.getUserById(id)
            val user = response.body() ?: throw ApiException(response.code(), response.message())
            listData.add(user)
            _users.value = listData
            return user
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )
            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun saveUserAvatar(user: User, upload: MediaUpload) {
        try {
            val media = upload(upload)
            user.avatar = (media.url)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }


}