/*
 * Copyright (c) 2020 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.di

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobWorkItem
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.global.device.ContextDeviceInfo
import com.duckduckgo.app.global.device.DeviceInfo
import com.duckduckgo.app.global.exception.UncaughtExceptionRepository
import com.duckduckgo.app.job.AndroidJobCleaner
import com.duckduckgo.app.job.AndroidWorkScheduler
import com.duckduckgo.app.job.ConfigurationDownloader
import com.duckduckgo.app.job.JobCleaner
import com.duckduckgo.app.job.WorkScheduler
import com.duckduckgo.app.notification.AndroidNotificationScheduler
import com.duckduckgo.app.referral.AppInstallationReferrerStateListener
import com.duckduckgo.app.statistics.AtbInitializer
import com.duckduckgo.app.statistics.api.OfflinePixelSender
import com.duckduckgo.app.statistics.api.PixelService
import com.duckduckgo.app.statistics.api.StatisticsService
import com.duckduckgo.app.statistics.api.StatisticsUpdater
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.store.OfflinePixelCountDataStore
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class StubAppComponentModule {

    @Provides
    fun stubPixel(): Pixel {
        return object : Pixel {

            override fun fire(pixel: Pixel.PixelName, parameters: Map<String, String>, encodedParameters: Map<String, String>) {
            }

            override fun fire(pixelName: String, parameters: Map<String, String>, encodedParameters: Map<String, String>) {
            }

            override fun fireCompletable(pixelName: String, parameters: Map<String, String>, encodedParameters: Map<String, String>): Completable {
                return Completable.fromAction {}
            }
        }
    }

    @Provides
    fun stubStatisticsUpdater(): StatisticsUpdater {
        return object : StatisticsUpdater {

            override fun initializeAtb() {
            }

            override fun refreshAppRetentionAtb() {
            }

            override fun refreshSearchRetentionAtb() {
            }

        }
    }
    @Singleton
    @Provides
    fun providesJobScheduler(): JobScheduler {
        return object : JobScheduler() {
            override fun enqueue(job: JobInfo?, work: JobWorkItem?): Int = JobScheduler.RESULT_SUCCESS

            override fun schedule(job: JobInfo?): Int = JobScheduler.RESULT_SUCCESS

            override fun cancel(jobId: Int) {}

            override fun cancelAll() {}

            override fun getAllPendingJobs(): MutableList<JobInfo> = mutableListOf()

            override fun getPendingJob(jobId: Int): JobInfo? = null

        }
    }

    @Singleton
    @Provides
    fun providesJobCleaner(workManager: WorkManager): JobCleaner {
        return AndroidJobCleaner(workManager)
    }

    @Singleton
    @Provides
    fun providesWorkScheduler(notificationScheduler: AndroidNotificationScheduler, jobCleaner: JobCleaner): WorkScheduler {
        return AndroidWorkScheduler(notificationScheduler, jobCleaner)
    }

    @Provides
    fun pixelService(@Named("nonCaching") retrofit: Retrofit): PixelService {
        return retrofit.create(PixelService::class.java)
    }

    @Provides
    fun offlinePixelSender(
        offlinePixelCountDataStore: OfflinePixelCountDataStore,
        uncaughtExceptionRepository: UncaughtExceptionRepository,
        pixel: Pixel
    ): OfflinePixelSender = OfflinePixelSender(offlinePixelCountDataStore, uncaughtExceptionRepository, pixel)

    @Provides
    @Singleton
    fun atbInitializer(
        statisticsDataStore: StatisticsDataStore,
        statisticsUpdater: StatisticsUpdater,
        appReferrerStateListener: AppInstallationReferrerStateListener
    ): AtbInitializer {
        return AtbInitializer(statisticsDataStore, statisticsUpdater, appReferrerStateListener)
    }

    @Provides
    fun statisticsService(retrofit: Retrofit): StatisticsService =
        retrofit.create(StatisticsService::class.java)

    @Provides
    fun deviceInfo(@ApplicationContext context: Context): DeviceInfo = ContextDeviceInfo(context)


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun fakeAppConfigurationDownloader(): ConfigurationDownloader {
        return object : ConfigurationDownloader {
            override fun downloadTask(): Completable {
                return Completable.complete()
            }
        }
    }
}