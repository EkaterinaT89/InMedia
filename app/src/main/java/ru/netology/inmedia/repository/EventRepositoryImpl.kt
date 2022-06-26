package ru.netology.inmedia.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.inmedia.api.ApiService
import ru.netology.inmedia.dto.*
import ru.netology.inmedia.entity.PostEntity
import ru.netology.inmedia.entity.toDto
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.enumeration.EventType
import ru.netology.inmedia.error.ApiException
import ru.netology.inmedia.error.AppError
import ru.netology.inmedia.error.NetWorkException
import ru.netology.inmedia.error.UnknownException
import java.io.IOException

class EventRepositoryImpl() : EventRepository {

    val listData = mutableListOf<Event>()

    private val _events = MutableLiveData<List<Event>>()

    override val data = _events.asFlow().flowOn(Dispatchers.Default)

    override suspend fun getAllEvents() {
        try {
            val response = ApiService.Api.retrofitService.getAllEvents()
            val events = response.body() ?: throw ApiException(response.code(), response.message())
            for (event in events) {
                listData.add(event)
            }
            _events.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun retryGetAllEvents() {
        try {
            val response = ApiService.Api.retrofitService.getAllEvents()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun createNewEvent(event: Event) {
        try {
            val response = ApiService.Api.retrofitService.createNewEvent(event)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val newEvent = response.body() ?: throw ApiException(response.code(), response.message())
            listData.add(newEvent)
            _events.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun getEventById(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.getEventById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun editEvent(event: Event) {
        try {
            val response = ApiService.Api.retrofitService.editEvent(event)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun likeEventById(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.likeEventById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun disLikeEventById(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.disLikeEventById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun removeEventById(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.removeEventById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun takePartEvent(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.takePartEvent(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun unTakePartEvent(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.unTakePartEvent(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun saveWithAttachment(
        event: Event,
        upload: MediaUpload
    ) {
        try {
            val media = upload(upload)
            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            createNewEvent(eventWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )
            val response = ApiService.Api.retrofitService.upload(media)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }

            return response.body() ?: throw ApiException(response.code(), response.message())
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

}