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

import androidx.lifecycle.ViewModel
import com.duckduckgo.app.global.SingleLiveEvent
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.survey.db.PushSurvey
import com.duckduckgo.app.survey.db.PushSurveyDao
import com.duckduckgo.app.survey.worker.PushSurveySubmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PushSurveyViewModel(
    private val pixel: Pixel,
    private val dao: PushSurveyDao,
    private val pushSurveySubmitter: PushSurveySubmitter
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
        val pushSurvey = PushSurvey(q1 = q1Answer ?: "", q2 = q2Answer ?: "", sentCount = 0, lastSent = System.currentTimeMillis())
        GlobalScope.launch(Dispatchers.IO) {
            dao.insert(pushSurvey)
        }
        pushSurveySubmitter.send(pushSurvey)
        command.value = Command.ShowSuccessMessage
        command.value = Command.Close
    }

    fun onSurveyDismissed() {
        pixel.fire(Pixel.PixelName.PUSH_SURVEY_DISMISSED)
        command.value = Command.Close
    }
}
