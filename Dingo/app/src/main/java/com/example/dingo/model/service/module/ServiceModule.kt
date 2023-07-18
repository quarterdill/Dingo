package com.example.dingo.model.service.module

import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.DingoDexEntryService
import com.example.dingo.model.service.DingoDexStorageService
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.TripService
import com.example.dingo.model.service.UserService
import com.example.dingo.model.service.impl.AccountServiceImpl
import com.example.dingo.model.service.impl.ClassroomServiceImpl
import com.example.dingo.model.service.impl.DingoDexEntryServiceImpl
import com.example.dingo.model.service.impl.DingoDexStorageServiceImpl
import com.example.dingo.model.service.impl.PostServiceImpl
import com.example.dingo.model.service.impl.TripServiceImpl
import com.example.dingo.model.service.impl.UserServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds abstract fun provideAccountService(impl: AccountServiceImpl): AccountService

    @Binds abstract fun provideDingoDexStorageService(impl: DingoDexStorageServiceImpl): DingoDexStorageService

    @Binds abstract fun provideClassroomService(impl: ClassroomServiceImpl): ClassroomService

    @Binds abstract fun provideUserService(impl: UserServiceImpl): UserService

    @Binds abstract fun providePostService(impl: PostServiceImpl): PostService

    @Binds abstract fun provideTripService(impl: TripServiceImpl): TripService
    @Binds abstract fun provideDingoDexEntryService(impl: DingoDexEntryServiceImpl): DingoDexEntryService
}