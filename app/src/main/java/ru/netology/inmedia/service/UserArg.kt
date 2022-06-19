package ru.netology.inmedia.service

import android.os.Bundle
import ru.netology.inmedia.dto.User
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object UserArg: ReadWriteProperty<Bundle, User?> {

    override fun getValue(thisRef: Bundle, property: KProperty<*>): User? =
        thisRef.getParcelable(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: User?) {
        thisRef.putParcelable(property.name, value)
    }
}