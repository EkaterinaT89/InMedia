package ru.netology.inmedia.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.netology.inmedia.enumeration.EventType
import java.time.Instant

@Parcelize
data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    /**
     * Дата и время проведения
     */
    val datetime: Instant? = null,
    var published: Instant? = null,
    /**
     * Координаты проведения
     */
    val coords: Coordinates? = null,
    /**
     * Типы события
     */
    val type: EventType? = null,
    /**
     * Id'шники залайкавших
     */
    val likeOwnerIds: Set<Long> = emptySet(),
    /**
     * Залайкал ли я
     */
    val likedByMe: Boolean = false,
    /**
     * Id'шники спикеров
     */
    val speakerIds: Set<Long> = emptySet(),
    /**
     * Id'шники участников
     */
    val participantsIds: Set<Long> = emptySet(),
    /**
     * Участвовал ли я
     */
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
): Parcelable