package ru.netology.inmedia.dto

import ru.netology.inmedia.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
)