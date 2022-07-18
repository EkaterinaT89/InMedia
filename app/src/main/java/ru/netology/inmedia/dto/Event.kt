package ru.netology.inmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.netology.inmedia.enumeration.EventType

@Parcelize
data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    var datetime: String,
    var published: String,
    val coords: Coordinates? = null,
    var type: EventType? = null,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: Set<Long> = emptySet(),
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
): Parcelable