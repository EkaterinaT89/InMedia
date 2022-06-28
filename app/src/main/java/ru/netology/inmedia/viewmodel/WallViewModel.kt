package ru.netology.inmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.inmedia.enumeration.ActionType
import ru.netology.inmedia.model.FeedModelState
import ru.netology.inmedia.repository.WallRepository
import javax.inject.Inject

@HiltViewModel
class WallViewModel @Inject constructor(
    val wallRepository: WallRepository
) : ViewModel() {

    val data = wallRepository.data.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    var lastId: Long? = null

    var lastAuthorId: Long? = null

    var lastAction: ActionType? = null

    fun tryAgain() {
        when (lastAction) {
            ActionType.GETWALL -> retryGetWall()
            ActionType.LIKEBYID -> retryLike()
            ActionType.DISLIKEBYID -> retryDisLike()
            else -> retryGetWall()
        }
    }

    fun getWall(id: Long) {
        viewModelScope.launch {
            lastAction = ActionType.GETWALL
            lastId = id
            try {
                _dataState.postValue(FeedModelState(loading = true))
                wallRepository.getUserWall(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
            lastAction = null
            lastId = null
        }
    }

    fun retryGetWall() {
        lastId?.let {
            getWall(it)
        }
    }

    fun likePostsOnWall(authorId: Long, postId: Long) {
        viewModelScope.launch {
            lastAction = ActionType.LIKEBYID
            lastId = postId
            lastAuthorId = authorId
            try {
                _dataState.postValue(FeedModelState(loading = true))
                wallRepository.likePostsOnWall(authorId, postId)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
            lastAction = null
            lastId = null
            lastAuthorId = null
        }
    }

    fun retryLike() {
        lastAuthorId?.let { lastId?.let { it1 -> likePostsOnWall(it, it1) } }
    }

    fun disLikePostsOnWall(authorId: Long, postId: Long) {
        viewModelScope.launch {
            lastAction = ActionType.LIKEBYID
            lastId = postId
            lastAuthorId = authorId
            try {
                _dataState.postValue(FeedModelState(loading = true))
                wallRepository.likePostsOnWall(authorId, postId)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
            lastAction = null
            lastId = null
            lastAuthorId = null
        }
    }

    fun retryDisLike() {
        lastAuthorId?.let { lastId?.let { it1 -> likePostsOnWall(it, it1) } }
    }


}