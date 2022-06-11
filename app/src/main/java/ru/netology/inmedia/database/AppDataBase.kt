package ru.netology.inmedia.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.inmedia.dao.PostDao
import ru.netology.inmedia.dao.PostRemoteKeyDao
import ru.netology.inmedia.entity.PostEntity
import ru.netology.inmedia.entity.PostRemoteKeyEntity


@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class],
    version = 1,
    exportSchema = false)

abstract class AppDataBase: RoomDatabase() {
    abstract fun postDao(): PostDao

    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}