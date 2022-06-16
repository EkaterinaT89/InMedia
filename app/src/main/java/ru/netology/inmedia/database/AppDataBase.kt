package ru.netology.inmedia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.inmedia.dao.PostDao
import ru.netology.inmedia.dao.PostRemoteKeyDao
import ru.netology.inmedia.entity.PostEntity
import ru.netology.inmedia.entity.PostRemoteKeyEntity


@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,AppDataBase::class.java, "app.db")
            .build()
    }

}