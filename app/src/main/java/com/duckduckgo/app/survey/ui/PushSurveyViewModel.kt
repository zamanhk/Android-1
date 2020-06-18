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

package com.duckduckgo.app.survey.ui

import android.os.Build
import androidx.lifecycle.ViewModel
import com.duckduckgo.app.global.SingleLiveEvent
import com.duckduckgo.app.global.install.AppInstallStore
import com.duckduckgo.app.global.install.daysInstalled
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.store.StatisticsDataStore

class PushSurveyViewModel(
    private val statisticsStore: StatisticsDataStore,
    private val appInstallStore: AppInstallStore,
    private val pixel: Pixel
) : ViewModel() {

    sealed class Command {
        object EnableSubmission : Command()
        object ShowSuccessMessage : Command()
        object Close : Command()
    }

    val command: SingleLiveEvent<Command> = SingleLiveEvent()
    private var q1Answer: String? = null
    private var q2Answer: String? = null

    fun onQ1Answered(answer: String) {
        q1Answer = answer
        validateForm()
    }

    fun onQ2Answered(answer: String) {
        q2Answer = answer
        validateForm()
    }

    private fun validateForm() {
        if (q2Answer != null && q2Answer != null) {
            command.value = Command.EnableSubmission
        }
    }

    fun onSubmitPressed() {
        val params = mapOf(
            Params.Q1 to (q1Answer ?: ""),
            Params.Q2 to (q2Answer ?: ""),
            Params.DAYS_INSTALLED to "${appInstallStore.daysInstalled()}",
            Params.ANDROID_VERSION to "${Build.VERSION.SDK_INT}",
            Params.MANUFACTURER to Build.MANUFACTURER,
            Params.MODEL to Build.MODEL
        )
        pixel.fire(Pixel.PixelName.PUSH_SURVEY_SUBMITTED, parameters = params)
        command.value = Command.ShowSuccessMessage
        command.value = Command.Close
    }

    fun onSurveyDismissed() {
        pixel.fire(Pixel.PixelName.PUSH_SURVEY_DISMISSED)
        command.value = Command.Close
    }

    private object Params {
        const val Q1 = "q1"
        const val Q2 = "q2"
        const val DAYS_INSTALLED = "delta"
        const val ANDROID_VERSION = "av"
        const val MANUFACTURER = "man"
        const val MODEL = "mo"
    }
}
