package ru.netology.inmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.inmedia.dto.Attachment
import ru.netology.inmedia.dto.Coordinates
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.AttachmentType

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
//    @Embedded
//    val coords: CoordsEmbeddable?,
    val link: String? = null,
//    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean = false,
//    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,

    @Embedded
    var attachment: AttachmentEmbeddable?
) {

    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        content,
        published,
//        coords?.toDto(),
        link = link,
//        mentionIds,
        mentionedMe = false,
//        likeOwnerIds,
        likedByMe = false,
        attachment = attachment?.toDto()
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
//                CoordsEmbeddable.fromDto(dto.coords),
                dto.link,
//                dto.mentionIds,
                dto.mentionedMe,
//                dto.likeOwnerIds,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment)
            )
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

data class CoordsEmbeddable(
    val lat: Double,
    val long: Double
) {
    fun toDto() = Coordinates(lat, long)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordsEmbeddable(it.lat, it.long)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)
