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

import android.app.job.JobScheduler
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.global.db.MigrationsProvider
import com.duckduckgo.app.global.device.ContextDeviceInfo
import com.duckduckgo.app.global.device.DeviceInfo
import com.duckduckgo.app.global.exception.UncaughtExceptionRepository
import com.duckduckgo.app.httpsupgrade.api.HttpsUpgradeDataDownloader
import com.duckduckgo.app.job.AndroidJobCleaner
import com.duckduckgo.app.job.AndroidWorkScheduler
import com.duckduckgo.app.job.AppConfigurationDownloader
import com.duckduckgo.app.job.ConfigurationDownloader
import com.duckduckgo.app.job.JobCleaner
import com.duckduckgo.app.job.WorkScheduler
import com.duckduckgo.app.notification.AndroidNotificationScheduler
import com.duckduckgo.app.referral.AppInstallationReferrerStateListener
import com.duckduckgo.app.statistics.AtbInitializer
import com.duckduckgo.app.statistics.VariantManager
import com.duckduckgo.app.statistics.api.OfflinePixelSender
import com.duckduckgo.app.statistics.api.PixelService
import com.duckduckgo.app.statistics.api.StatisticsRequester
import com.duckduckgo.app.statistics.api.StatisticsService
import com.duckduckgo.app.statistics.api.StatisticsUpdater
import com.duckduckgo.app.statistics.pixels.ApiBasedPixel
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.store.OfflinePixelCountDataStore
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.app.surrogates.api.ResourceSurrogateListDownloader
import com.duckduckgo.app.survey.api.SurveyDownloader
import com.duckduckgo.app.trackerdetection.api.TrackerDataDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class AppComponentModule {

    @Provides
    fun pixel(pixelService: PixelService, statisticsDataStore: StatisticsDataStore, variantManager: VariantManager, deviceInfo: DeviceInfo): Pixel =
        ApiBasedPixel(pixelService, statisticsDataStore, variantManager, deviceInfo)

    @Provides
    fun statisticsUpdater(
        statisticsDataStore: StatisticsDataStore,
        statisticsService: StatisticsService,
        variantManager: VariantManager
    ): StatisticsUpdater =
        StatisticsRequester(statisticsDataStore, statisticsService, variantManager)

    @Singleton
    @Provides
    fun providesJobScheduler(@ApplicationContext context: Context): JobScheduler {
        return context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
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
    @Singleton
    fun atbInitializer(
        statisticsDataStore: StatisticsDataStore,
        statisticsUpdater: StatisticsUpdater,
        appReferrerStateListener: AppInstallationReferrerStateListener
    ): AtbInitializer {
        return AtbInitializer(statisticsDataStore, statisticsUpdater, appReferrerStateListener)
    }

    @Provides
    fun statisticsService(@Named("api") retrofit: Retrofit): StatisticsService = retrofit.create(StatisticsService::class.java)


    @Provides
    fun deviceInfo(@ApplicationContext context: Context): DeviceInfo = ContextDeviceInfo(context)

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
    fun provideDatabase(@ApplicationContext context: Context, migrationsProvider: MigrationsProvider): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
            .addMigrations(*migrationsProvider.ALL_MIGRATIONS.toTypedArray())
            .build()
    }

    @Provides
    open fun appConfigurationDownloader(
        trackerDataDownloader: TrackerDataDownloader,
        httpsUpgradeDataDownloader: HttpsUpgradeDataDownloader,
        resourceSurrogateDownloader: ResourceSurrogateListDownloader,
        surveyDownloader: SurveyDownloader
    ): ConfigurationDownloader {

        return AppConfigurationDownloader(
            trackerDataDownloader,
            httpsUpgradeDataDownloader,
            resourceSurrogateDownloader,
            surveyDownloader
        )
    }

}