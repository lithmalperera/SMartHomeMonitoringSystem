package com.smarthome.monitor.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.smarthome.monitor.data.repository.ActivityRepositoryImpl
import com.smarthome.monitor.data.repository.AuthRepositoryImpl
import com.smarthome.monitor.data.repository.FloorRepositoryImpl
import com.smarthome.monitor.domain.repository.ActivityRepository
import com.smarthome.monitor.domain.repository.AuthRepository
import com.smarthome.monitor.domain.repository.FloorRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.smarthome.monitor.data.repository.DeviceRepositoryImpl
import com.smarthome.monitor.domain.repository.DeviceRepository

import com.smarthome.monitor.data.repository.UsageRepositoryImpl
import com.smarthome.monitor.domain.repository.UsageRepository

import com.smarthome.monitor.data.repository.SettingsRepositoryImpl
import com.smarthome.monitor.domain.repository.SettingsRepository

import com.smarthome.monitor.data.repository.AlertRepositoryImpl
import com.smarthome.monitor.domain.repository.AlertRepository

import com.smarthome.monitor.data.repository.ScheduleRepositoryImpl
import com.smarthome.monitor.domain.repository.ScheduleRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFloorRepository(
        impl: FloorRepositoryImpl
    ): FloorRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        impl: ActivityRepositoryImpl
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl
    ): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindUsageRepository(
        impl: UsageRepositoryImpl
    ): UsageRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        impl: AlertRepositoryImpl
    ): AlertRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseDatabase(): FirebaseDatabase {
            return FirebaseDatabase.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }
    }
}
