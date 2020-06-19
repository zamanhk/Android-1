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

package com.duckduckgo.app.survey.worker

import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.duckduckgo.app.global.install.AppInstallStore
import com.duckduckgo.app.global.install.daysInstalled
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.survey.db.PushSurvey
import com.duckduckgo.app.survey.db.PushSurveyDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PushSurveySubmitter(
    private val appInstallStore: AppInstallStore,
    private val pixel: Pixel
) {

    fun send(survey: PushSurvey) {
        val params = mapOf(
            Params.Q1 to (survey.q1),
            Params.Q2 to (survey.q2),
            Params.RECORD_COUNT to "${survey.sentCount}",
            Params.DAYS_INSTALLED to "${appInstallStore.daysInstalled()}",
            Params.ANDROID_VERSION to "${Build.VERSION.SDK_INT}",
            Params.MANUFACTURER to Build.MANUFACTURER,
            Params.MODEL to Build.MODEL
        )
        pixel.fire(Pixel.PixelName.PUSH_SURVEY_SUBMITTED, parameters = params)
    }

    private object Params {
        const val Q1 = "q1"
        const val Q2 = "q2"
        const val RECORD_COUNT = "rc"
        const val DAYS_INSTALLED = "delta"
        const val ANDROID_VERSION = "av"
        const val MANUFACTURER = "man"
        const val MODEL = "mo"
    }
}

class PushSurveyRepeatSubmitter(val dao: PushSurveyDao, val submitter: PushSurveySubmitter) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun sendPushSurvey() {

        //TODO test and make this thread safe to safeguard from double submissions
        GlobalScope.launch(Dispatchers.IO) {
            val survey = dao.get()
            if (survey == null) {
                Timber.v("Push survey: was never submitted, nothing to send")
                return@launch
            }

            val currentTimestamp = System.currentTimeMillis()
            val nextSendTimestamp = survey.lastSent + TimeUnit.HOURS.toMillis(1)
            if (currentTimestamp < nextSendTimestamp) {
                Timber.v("Push survey: Schedule not yet reached for next send")
                return@launch
            }

            Timber.v("Push survey: Sending repeat pixel now")
            val latestSurvey = PushSurvey(q1 = survey.q1, q2 = survey.q2, sentCount = survey.sentCount + 1, lastSent = currentTimestamp)
            dao.update(latestSurvey)
            submitter.send(latestSurvey)
        }
    }

    companion object {  
        private const val WORK_REQUEST_TAG = "com.duckduckgo.app.survey.pushsurvey.schedule"
        private const val SERVICE_INTERVAL = 1L
        private val SERVICE_TIME_UNIT = TimeUnit.HOURS
        private const val BACKOFF_INTERVAL = 10L
        private val BACKOFF_TIME_UNIT = TimeUnit.MINUTES
    }
}