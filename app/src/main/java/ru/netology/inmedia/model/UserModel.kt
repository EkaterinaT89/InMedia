package ru.netology.inmedia.model

import ru.netology.inmedia.dto.User

data class UserModel(
    val users: List<User> = emptyList(),
    val empty: Boolean = false,
)