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
import ru.netology.inmedia.dto.*
import ru.netology.inmedia.enumeration.ActionType
import ru.netology.inmedia.enumeration.EventType
import ru.netology.inmedia.model.AttachmentModel
import ru.netology.inmedia.model.FeedEventModel
import ru.netology.inmedia.model.FeedModel
import ru.netology.inmedia.model.FeedModelState
import ru.netology.inmedia.repository.EventRepository
import ru.netology.inmedia.repository.EventRepositoryImpl
import ru.netology.inmedia.repository.PostRepository
import ru.netology.inmedia.repository.PostRepositoryImpl
import ru.netology.inmedia.service.SingleLiveEvent
import java.io.File
import java.time.Instant

private val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    datetime = null,
    published = null,
    coords = null,
    type = null,
    likeOwnerIds = emptySet(),
    speakerIds = emptySet(),
    participantsIds = emptySet(),
    participatedByMe = false,
    attachment = null,
    link = null
)

class EventViewModel() : ViewModel() {

    private val repository: EventRepository =
        EventRepositoryImpl()

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(emptyEvent)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    var currentId: Long? = null

    var currentEvent: Event? = null

    var lastAction: ActionType? = null

    private val noAttachment = AttachmentModel()

    private val _attachment = MutableLiveData(noAttachment)
    val photo: LiveData<AttachmentModel>
        get() = _attachment

    @ExperimentalCoroutinesApi
    val data: LiveData<FeedEventModel> = AppAuth.getInstance()
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { events ->
                    FeedEventModel(
                        events.map { it.copy(ownedByMe = it.authorId == myId) },
                        events.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    fun tryAgain() {
        when (lastAction) {
            ActionType.LOAD -> retryGetAllEvents()
            ActionType.GETBYID -> retryGetEventById()
            ActionType.EDIT -> retryEditEvent()
            ActionType.LIKEBYID -> retryLikeById()
            ActionType.DISLIKEBYID -> retryDisLikeById()
            ActionType.TAKEPARTEVENT -> retryTakePartEvent()
            ActionType.UNTAKEPARTEVENT -> retryUnTakePartEvent()
            else -> retryGetAllEvents()
        }
    }

    init {
        getAllEvents()
    }

    fun getAllEvents() = viewModelScope.launch {
        lastAction = ActionType.LOAD
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.getAllEvents()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
        lastAction = null
    }

    fun retryGetAllEvents() {
        getAllEvents()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNewEvent() {
        lastAction = ActionType.SAVE
        edited.value?.let {
            _eventCreated.value = Unit
            it.published = Instant.now()
            viewModelScope.launch {
                try {
                    when (_attachment.value) {
                        noAttachment -> repository.createNewEvent(it)
                        else -> _attachment.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = emptyEvent
        _attachment.value = noAttachment
        lastAction = null
    }

    fun getEventById(id: Long) {
        lastAction = ActionType.GETBYID
        currentId = id
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.getEventById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentId = null
    }

    fun retryGetEventById() {
        currentId?.let {
            getEventById(it)
        }
    }

    fun editEvent(event: Event) {
        lastAction = ActionType.EDIT
        currentEvent = event
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.editEvent(event)
                edited.value = event
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentEvent = null
    }

    fun retryEditEvent() {
        currentEvent?.let {
            editEvent(it)
        }
    }

    fun editEventContent(text: String) {
        val formatted = text.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = formatted)
    }

    fun likeById(id: Long) {
        lastAction = ActionType.LIKEBYID
        currentId = id
        viewModelScope.launch {
            try {
                repository.likeEventById(id)
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
        lastAction = ActionType.DISLIKEBYID
        currentId = id
        viewModelScope.launch {
            try {
                repository.disLikeEventById(id)
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

    fun removeEventById(id: Long) {
        lastAction = ActionType.REMOVEBYID
        currentId = id
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.disLikeEventById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentId = null
    }

    fun retryRemoveEventById() {
        currentId?.let {
            removeEventById(it)
        }
    }

    fun takePartEvent(id: Long) {
        lastAction = ActionType.TAKEPARTEVENT
        currentId = id
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.takePartEvent(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentId = null
    }

    fun retryTakePartEvent() {
        currentId?.let {
            takePartEvent(it)
        }
    }

    fun unTakePartEvent(id: Long) {
        lastAction = ActionType.UNTAKEPARTEVENT
        currentId = id
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.unTakePartEvent(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentId = null
    }

    fun retryUnTakePartEvent() {
        currentId?.let {
            unTakePartEvent(it)
        }
    }

    fun changeAttachment(uri: Uri?, file: File?) {
        _attachment.value = AttachmentModel(uri, file)
    }

}