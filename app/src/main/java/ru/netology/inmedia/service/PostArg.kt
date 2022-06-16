package ru.netology.inmedia.service

import android.os.Bundle
import ru.netology.inmedia.dto.Post
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object PostArg: ReadWriteProperty<Bundle, Post?> {

    override fun getValue(thisRef: Bundle, property: KProperty<*>): Post? =
        thisRef.getParcelable(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Post?) {
        thisRef.putParcelable(property.name, value)
    }
}