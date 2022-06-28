package ru.netology.inmedia.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import ru.netology.inmedia.api.ApiService
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.error.ApiException
import ru.netology.inmedia.error.NetWorkException
import ru.netology.inmedia.error.UnknownException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : JobRepository {

    override val listData = mutableListOf<Job>()

    private val _jobs = MutableLiveData<List<Job>>()

    override val data = _jobs.asFlow().flowOn(Dispatchers.Default)

    override suspend fun getAllJobs(id: Long) {
        try {
            val response = apiService.getAllJobs(id)
            val jobs = response.body() ?: throw ApiException(response.code(), response.message())
            for (job in jobs) {
                if(!listData.contains(job)) {
                    listData.add(job)
                }
            }
            _jobs.value = listData
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun createNewJob(job: Job) {
        try {
            val response = apiService.createNewJob(job)
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
            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetWorkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun editJob(job: Job) {
        try {
            val response = apiService.editJob(job)
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