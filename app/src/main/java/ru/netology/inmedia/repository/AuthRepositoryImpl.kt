package ru.netology.inmedia.repository

import ru.netology.inmedia.api.ApiService
import ru.netology.inmedia.auth.AppAuth.Companion.getInstance
import ru.netology.inmedia.error.ApiException
import ru.netology.inmedia.error.NetWorkException
import ru.netology.inmedia.error.UnknownException
import java.io.IOException

class AuthRepositoryImpl: AuthRepository {
    override suspend fun signIn(login: String, pass: String) {
        try {
            val response = ApiService.Api.retrofitService.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val authState = response.body() ?: throw ApiException(response.code(), response.message())
            authState.token?.let { getInstance().setAuth(authState.id, it) }

        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
        return
    }

    override suspend fun signUp(name: String, login: String, pass: String) {
        try {
            val response = ApiService.Api.retrofitService.registerUser(name, login, pass)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val authState = response.body() ?: throw ApiException(response.code(), response.message())
            authState.token?.let { getInstance().setAuth(authState.id, it) }

        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}