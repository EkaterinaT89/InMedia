package ru.netology.inmedia.model

import ru.netology.inmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)