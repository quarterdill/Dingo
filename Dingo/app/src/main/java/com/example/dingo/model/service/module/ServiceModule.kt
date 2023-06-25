package com.example.dingo.model.service.module

import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.DingoDexCollectionStorageService
import com.example.dingo.model.service.DingoDexStorageService
import com.example.dingo.model.service.impl.AccountServiceImpl
import com.example.dingo.model.service.impl.ClassroomServiceImpl
import com.example.dingo.model.service.impl.DingoDexCollectionStorageServiceImpl
import com.example.dingo.model.service.impl.DingoDexStorageServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds abstract fun provideAccountService(impl: AccountServiceImpl): AccountService

    @Binds abstract fun DingoDexCollectionStorageService(impl: DingoDexCollectionStorageServiceImpl): DingoDexCollectionStorageService

    @Binds abstract fun provideDingoDexStorageService(impl: DingoDexStorageServiceImpl): DingoDexStorageService

    @Binds abstract fun provideClassroomService(impl: ClassroomServiceImpl): ClassroomService
}