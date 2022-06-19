package ru.netology.inmedia.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.inmedia.dto.User
import ru.netology.inmedia.enumeration.ActionType
import ru.netology.inmedia.model.FeedModelState
import ru.netology.inmedia.repository.UserRepository
import ru.netology.inmedia.repository.UserRepositoryImpl

class UserViewModel() : ViewModel() {

    val repository: UserRepository = UserRepositoryImpl()

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    var lastAction: ActionType? = null

    val data = repository.data.asLiveData(Dispatchers.Default)

    val wallData = repository.wall.asLiveData(Dispatchers.Default)

    var lastId: Long? = null

    var lastAuthorId: Long? = null

    fun tryAgain() {
        when (lastAction) {
            ActionType.LOAD -> retryGetAllUsers()
            ActionType.GETBYID -> retryGetById()
            else -> retryGetAllUsers()
        }
    }

    init {
        getAllUsers()
    }

    fun getAllUsers() = viewModelScope.launch {
        lastAction = ActionType.LOAD
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.getAllUsers()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
        lastAction = null
    }

    fun retryGetAllUsers() {
        getAllUsers()
    }

    fun getUserById(id: Long) {
        viewModelScope.launch {
            lastAction = ActionType.GETBYID
            lastId = id
            try {
                _dataState.postValue(FeedModelState(loading = true))
                repository.getUserById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
            lastAction = null
            lastId = null
        }

    }

    fun retryGetById() {
        lastId?.let {
            getUserById(it)
        }
    }





}

