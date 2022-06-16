package ru.netology.inmedia.model

import ru.netology.inmedia.dto.Event

class FeedEventModel(
    val events: List<Event> = emptyList(),
    val empty: Boolean = false,
)