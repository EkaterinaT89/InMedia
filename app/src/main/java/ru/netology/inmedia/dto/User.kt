package ru.netology.inmedia.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: Long,
    val login: String,
    val name: String,
    var avatar: String? = null
): Parcelable