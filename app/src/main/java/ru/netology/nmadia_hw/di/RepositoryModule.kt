package ru.netology.nmadia_hw.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmadia_hw.repository.PostRepository
import ru.netology.nmadia_hw.repository.PostRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl,
    ): PostRepository
}