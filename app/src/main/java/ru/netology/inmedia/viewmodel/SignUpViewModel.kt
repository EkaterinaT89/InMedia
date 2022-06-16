package ru.netology.inmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.inmedia.auth.AppAuth
import ru.netology.inmedia.auth.AuthState
import ru.netology.inmedia.database.AppDataBase
import ru.netology.inmedia.repository.AuthRepository
import ru.netology.inmedia.repository.AuthRepositoryImpl
import ru.netology.inmedia.repository.PostRepository
import ru.netology.inmedia.repository.PostRepositoryImpl

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository =
        AuthRepositoryImpl()

    val data: LiveData<AuthState> = AppAuth.getInstance()
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = AppAuth.getInstance().authStateFlow.value.id != 0L

    fun signeUp(name: String, login: String, pass: String) {
        viewModelScope.launch {
            repository.signUp(name, login, pass)
        }
    }

}