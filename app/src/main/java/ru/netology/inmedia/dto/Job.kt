package ru.netology.inmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long? = null,
    val link: String? = null,
): Parcelable