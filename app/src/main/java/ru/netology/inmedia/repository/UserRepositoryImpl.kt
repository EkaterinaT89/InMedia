package ru.netology.inmedia.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import ru.netology.inmedia.api.ApiService
import ru.netology.inmedia.dto.Event
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.dto.User
import ru.netology.inmedia.error.ApiException
import ru.netology.inmedia.error.NetWorkException
import ru.netology.inmedia.error.UnknownException
import java.io.IOException

class UserRepositoryImpl : UserRepository {

    val listData = mutableListOf<User>()

    private val _users = MutableLiveData<List<User>>()

    override val data = _users.asFlow().flowOn(Dispatchers.Default)


    val wallList = mutableListOf<Post>()

    private val _wall = MutableLiveData<List<Post>>()

    override val wall = _wall.asFlow().flowOn(Dispatchers.Default)


    override suspend fun getAllUsers() {
        try {
            val response = ApiService.Api.retrofitService.getAllUsers()
            val users = response.body() ?: throw ApiException(response.code(), response.message())
            for (user in users) {
                listData.add(user)
            }
            _users.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun getUserById(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.getUserById(id)
            val user = response.body() ?: throw ApiException(response.code(), response.message())
            listData.add(user)
            _users.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }



}