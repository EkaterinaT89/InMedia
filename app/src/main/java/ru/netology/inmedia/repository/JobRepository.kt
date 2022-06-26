package ru.netology.inmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.inmedia.dto.Job

interface JobRepository {

    val data: Flow<List<Job>>

    val listData: MutableList<Job>

    suspend fun getAllJobs(id: Long)

    suspend fun createNewJob(job: Job)

    suspend fun removeJobById(id: Long)

    suspend fun editJob(job: Job)

}