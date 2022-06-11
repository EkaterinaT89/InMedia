package ru.netology.inmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.inmedia.auth.AppAuth
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.ActionType
import ru.netology.inmedia.model.FeedModel
import ru.netology.inmedia.model.FeedModelState
import ru.netology.inmedia.repository.PostRepository
import ru.netology.inmedia.service.SingleLiveEvent
import javax.inject.Inject

private val emptyPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    coords = null,
    link = "",
    mentionIds = emptySet(),
    mentionedMe = false,
    likeOwnerIds = emptySet(),
    likedByMe = false,
    attachment = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth
) : ViewModel() {

    val data: LiveData<FeedModel> = auth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

//    private val _photo = MutableLiveData(noPhoto)
//    val photo: LiveData<PhotoModel>
//        get() = _photo

//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }

    val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    var currentId: Long? = null

    var currentPost: Post? = null

    var lastAction: ActionType? = null

    init {
        loadPosts()
    }

    fun tryAgain() {
        when (lastAction) {
            ActionType.LIKEBYID -> retryLikeById()
            ActionType.DISLIKEBYID -> retryDisLikeById()
            ActionType.SAVE -> retrySave()
            ActionType.LOADPOSTS -> retryLoadPosts()
            ActionType.REMOVEBYID -> retryRemoveById()
            ActionType.GETBYID -> retryGetById()
            ActionType.EDIT -> retryEdit()
            else -> loadPosts()
        }
    }

    fun save() {
        lastAction = ActionType.SAVE
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
            edited.value = emptyPost
        }
    }

        fun retrySave() {
            save()
        }

        fun loadPosts() = viewModelScope.launch {
            lastAction = ActionType.LOADPOSTS
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.getAll()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }

        fun retryLoadPosts() {
            loadPosts()
        }

        fun getLastTen() =
            viewModelScope.launch {
                try {
                    _dataState.value = FeedModelState(loading = true)
                    repository.getLastTen()
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }

        fun getById(id: Long) =
            viewModelScope.launch {
                lastAction = ActionType.GETBYID
                currentId = id
                try {
                    _dataState.value = FeedModelState(loading = true)
                    repository.getById(id)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }

        fun retryGetById() {
            currentId?.let {
                getById(it)
            }
        }

        fun edit(post: Post) =
            viewModelScope.launch {
                lastAction = ActionType.GETBYID
                currentPost = post
                try {
                    _dataState.value = FeedModelState(loading = true)
                    repository.edit(post)
                    edited.value = post
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }

        fun retryEdit() {
            currentPost?.let {
                edit(it)
            }
        }

        fun editContent(text: String) {
            val formatted = text.trim()
            if (edited.value?.content == text) {
                return
            }
            edited.value = edited.value?.copy(content = formatted)
        }

        fun likeById(id: Long) {
            viewModelScope.launch {
                currentId = id
                lastAction = ActionType.LIKEBYID
                try {
                    repository.likeById(id)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }

        fun retryLikeById() {
            currentId?.let {
                likeById(it)
            }
        }

        fun disLikeById(id: Long) {
            viewModelScope.launch {
                currentId = id
                lastAction = ActionType.DISLIKEBYID
                try {
                    repository.disLikeById(id)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }

        fun retryDisLikeById() {
            currentId?.let {
                disLikeById(it)
            }
        }

        fun removeById(id: Long) {
            viewModelScope.launch {
                currentId = id
                lastAction = ActionType.REMOVEBYID
                try {
                    _dataState.value = FeedModelState()
                    repository.removeById(id)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }

        fun retryRemoveById() {
            currentId?.let {
                removeById(it)
            }
        }

        fun getPostNotExist(id: Long) {
            viewModelScope.launch {
                try {
                    _dataState.value = FeedModelState()
                    repository.getPostNotExist(id)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }


        fun refreshPosts() = viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(refreshing = true)
                repository.getAll()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }


//    fun save() {
//        lastAction = ActionType.SAVE
//        edited.value?.let {
//            _postCreated.value = Unit
//            viewModelScope.launch {
//                try {
//                    when (_photo.value) {
//                        noPhoto -> repository.save(it)
//                        else -> _photo.value?.file?.let { file ->
//                            repository.saveWithAttachment(it, MediaUpload(file))
//                        }
//                    }
//                    _dataState.value = FeedModelState()
//                } catch (e: Exception) {
//                    _dataState.value = FeedModelState(error = true)
//                }
//            }
//        }
//        edited.value = emptyPost
//        _photo.value = noPhoto
//    }

//        fun changePhoto(uri: Uri?, file: File?) {
//            _photo.value = PhotoModel(uri, file)
//        }



}




