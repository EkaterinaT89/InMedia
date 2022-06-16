package ru.netology.inmedia.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.inmedia.auth.AppAuth
import ru.netology.inmedia.database.AppDataBase
import ru.netology.inmedia.dto.MediaUpload
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.ActionType
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.model.FeedModel
import ru.netology.inmedia.model.FeedModelState
import ru.netology.inmedia.model.AttachmentModel
import ru.netology.inmedia.repository.PostRepository
import ru.netology.inmedia.repository.PostRepositoryImpl
import ru.netology.inmedia.service.SingleLiveEvent
import java.io.File
import java.time.Instant

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

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository =
        PostRepositoryImpl(AppDataBase.getInstance(context = application).postDao())

    @ExperimentalCoroutinesApi
    val data: LiveData<FeedModel> = AppAuth.getInstance()
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

    private val noAttachment = AttachmentModel()

    private val _attachment = MutableLiveData(noAttachment)
    val photo: LiveData<AttachmentModel>
        get() = _attachment


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

    var currentAttachment: AttachmentType? = null

    init {
        loadPosts()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun tryAgain() {
        when (lastAction) {
            ActionType.LIKEBYID -> retryLikeById()
            ActionType.DISLIKEBYID -> retryDisLikeById()
            ActionType.SAVE -> retrySave()
            ActionType.LOAD -> retryLoadPosts()
            ActionType.REMOVEBYID -> retryRemoveById()
            ActionType.GETBYID -> retryGetById()
            ActionType.EDIT -> retryEdit()
            else -> loadPosts()
        }
    }

    fun loadPosts() = viewModelScope.launch {
        lastAction = ActionType.LOAD
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
        lastAction = null
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
            lastAction = null
            currentId = null
        }

    fun retryGetById() {
        currentId?.let {
            getById(it)
        }
    }

    fun edit(post: Post) =
        viewModelScope.launch {
            lastAction = ActionType.EDIT
            currentPost = post
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.edit(post)
                edited.value = post
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
            lastAction = null
            currentPost = null
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
        lastAction = null
        currentId = null
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
        lastAction = null
        currentId = null
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
                _dataState.value = FeedModelState(loading = true)
                repository.removeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentId = null
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun save() {
        lastAction = ActionType.SAVE
        edited.value?.let {
            _postCreated.value = Unit
            it.published = Instant.now().toString()
            viewModelScope.launch {
                try {
                    when (_attachment.value) {
                        noAttachment -> repository.save(it)
                        else -> _attachment.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
                _postCreated.value = Unit
            }
        }
        edited.value = emptyPost
        _attachment.value = noAttachment
        lastAction = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun retrySave() {
        save()
    }

    fun changeAttachment(uri: Uri?, file: File?) {
        _attachment.value = AttachmentModel(uri, file)
    }
}






