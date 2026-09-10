package com.rafario.lahrecetah.di

import com.rafario.lahrecetah.data.repository.DataStoreSessionRepository
import com.rafario.lahrecetah.data.repository.FirebaseAuthRepository
import com.rafario.lahrecetah.data.repository.FirebaseRecipeRepository
import com.rafario.lahrecetah.data.repository.FirestoreUserRepository
import com.rafario.lahrecetah.domain.repository.AuthRepository
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.domain.repository.SessionRepository
import com.rafario.lahrecetah.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(implementation: FirebaseRecipeRepository): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(implementation: FirestoreUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(implementation: DataStoreSessionRepository): SessionRepository
}
