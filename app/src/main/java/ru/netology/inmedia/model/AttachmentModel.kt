package ru.netology.inmedia.model

import android.net.Uri
import ru.netology.inmedia.enumeration.AttachmentType
import java.io.File

class AttachmentModel(
    val uri: Uri? = null,
    val file: File? = null,
    val type: AttachmentType? = null
)