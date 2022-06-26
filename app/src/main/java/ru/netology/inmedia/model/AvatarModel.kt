package ru.netology.inmedia.model

import android.net.Uri
import ru.netology.inmedia.enumeration.AttachmentType
import java.io.File

class AvatarModel(
    val uri: Uri? = null,
    val file: File? = null
)