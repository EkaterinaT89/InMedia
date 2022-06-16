package ru.netology.inmedia.repository

interface AuthRepository {

    suspend fun signIn(login: String, pass: String)

    suspend fun signUp(name: String, login: String, pass: String)

}