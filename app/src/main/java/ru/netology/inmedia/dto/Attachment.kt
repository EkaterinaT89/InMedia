package ru.netology.inmedia.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.netology.inmedia.enumeration.AttachmentType

@Parcelize
data class Attachment(
    val url: String,
    val type: AttachmentType,
)  : Parcelable