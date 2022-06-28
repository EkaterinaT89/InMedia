package ru.netology.inmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.inmedia.auth.AppAuth
import ru.netology.inmedia.auth.AuthState
import ru.netology.inmedia.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: AuthRepository,
    auth: AppAuth
) : ViewModel() {

    val data: LiveData<AuthState> = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    fun signeUp(name: String, login: String, pass: String) {
        viewModelScope.launch {
            repository.signUp(name, login, pass)
        }
    }

}