package ru.netology.nmadia_hw.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.nmadia_hw.dao.PostDao
import ru.netology.nmadia_hw.db.AppDb
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun provideDb(
        @ApplicationContext context: Context,
    ): AppDb = Room.databaseBuilder(
        context,
        AppDb::class.java,
        "posts.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao()
}