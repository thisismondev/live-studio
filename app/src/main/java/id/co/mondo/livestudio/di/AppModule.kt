package id.co.mondo.livestudio.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.mondo.livestudio.data.repository.AuthRepositoryImpl
import id.co.mondo.livestudio.data.repository.LiveRepositoryImpl
import id.co.mondo.livestudio.domain.repository.AuthRepository
import id.co.mondo.livestudio.domain.repository.LiveRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLiveRepository(
        liveRepositoryImpl: LiveRepositoryImpl
    ): LiveRepository
}