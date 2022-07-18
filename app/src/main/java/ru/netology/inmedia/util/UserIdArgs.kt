package ru.netology.inmedia.util

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import ru.netology.inmedia.fragment.ListMyOccupationFragment
import ru.netology.inmedia.fragment.MyPageFragment

private const val ID_KEY = "ID_KEY"

fun MyPageFragment.setId(id: Int) {
    arguments = (arguments ?: Bundle()).apply { putLong(ID_KEY, id.toLong()) }
}

val SavedStateHandle.id: Long
    get() = get(ID_KEY) ?: 0