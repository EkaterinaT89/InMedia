package ru.netology.inmedia.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import ru.netology.inmedia.api.ApiService
import ru.netology.inmedia.dto.Event
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.error.ApiException
import ru.netology.inmedia.error.NetWorkException
import ru.netology.inmedia.error.UnknownException
import java.io.IOException

class JobRepositoryImpl : JobRepository {

    val listData = mutableListOf<Job>()

    private val _jobs = MutableLiveData<List<Job>>()

    override val data = _jobs.asFlow().flowOn(Dispatchers.Default)

    override suspend fun getAllJobs(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.getAllJobs(id)
            val jobs = response.body() ?: throw ApiException(response.code(), response.message())
            for (job in jobs) {
                listData.add(job)
            }
            _jobs.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun createNewJob(id: Long, job: Job) {
        try {
            val response = ApiService.Api.retrofitService.createNewJob(job, id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val jobBody =
                response.body() ?: throw ApiException(response.code(), response.message())
            val newJob = jobBody.copy()
            listData.add(newJob)
            _jobs.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun removeJobById(id: Long) {
        try {
            val response = ApiService.Api.retrofitService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun editJob(job: Job, id: Long) {
        try {
            val response = ApiService.Api.retrofitService.editJob(job, id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }


}