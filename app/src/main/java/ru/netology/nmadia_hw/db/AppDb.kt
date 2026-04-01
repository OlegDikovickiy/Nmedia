package ru.netology.nmadia_hw.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmadia_hw.dao.PostDao
import ru.netology.nmadia_hw.entity.PostEntity

@Database(entities = [PostEntity::class], version = 3, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
}