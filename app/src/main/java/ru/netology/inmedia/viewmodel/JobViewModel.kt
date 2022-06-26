package ru.netology.inmedia.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.enumeration.ActionType
import ru.netology.inmedia.model.FeedModelState
import ru.netology.inmedia.repository.JobRepository
import ru.netology.inmedia.repository.JobRepositoryImpl
import ru.netology.inmedia.service.SingleLiveEvent
import ru.netology.inmedia.util.DateFormatter
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant

private val emptyJob = Job(
    id = 0,
    name = "",
    position = "",
    start = 0,
    finish = null,
    link = null,
    userId = 0
)

class JobViewModel() : ViewModel() {

    private val repository: JobRepository =
        JobRepositoryImpl()

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(emptyJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    val data = repository.data.asLiveData(Dispatchers.Default)

    var currentId: Long? = null

    var currentJob: Job? = null

    var lastAction: ActionType? = null

    fun tryAgain() {
        when (lastAction) {
            ActionType.LOAD -> retryGetAllJobs()
            ActionType.EDIT -> retryEditJob()
            ActionType.REMOVEBYID -> retryRemoveJobById()
            else -> retryGetAllJobs()
        }
    }

    fun getAllJobs(id: Long) = viewModelScope.launch {
        lastAction = ActionType.LOAD
        currentId = id
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.getAllJobs(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
        lastAction = null
    }

    fun retryGetAllJobs() {
        currentId?.let {
            getAllJobs(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNewJob(userId: Long) {
        lastAction = ActionType.SAVE
        currentId = userId
        edited.value?.let {
            it.userId = userId
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    _dataState.postValue(FeedModelState(loading = true))
                    repository.createNewJob(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = emptyJob
        lastAction = null
        currentId = null
    }

    fun editJobContent(companyName: String, position: String, start: String, end: String) {
        val getCompanyName = companyName.trim()
        val getPosition = position.trim()
        val getStart = start.trim().toLong()
        val getEnd = end.trim().toLong()
        if (edited.value?.name == getCompanyName && edited.value?.position == getPosition
                    && edited.value?.start == getStart && edited.value?.finish == getEnd) {
            return
        }
        edited.value = edited.value?.copy(name = getCompanyName, position = getPosition,
                start = getStart, finish = getEnd)
    }

    fun editJob(job: Job) {
        lastAction = ActionType.EDIT
        currentJob = job
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.editJob(job)
                edited.value = job
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentJob = null
    }

    fun retryEditJob() {
        currentId?.let { id ->
            currentJob?.let { job ->
                editJob(job)
            }
        }
    }

    fun removeJobById(id: Long) {
        lastAction = ActionType.REMOVEBYID
        currentId = id
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.removeJobById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        lastAction = null
        currentId = null
    }

    fun retryRemoveJobById() {
        currentId?.let {
            removeJobById(it)
        }
    }

}