/*
 * Copyright (c) 2019 DuckDuckGo
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

package com.duckduckgo.app.notification.model

import android.content.Context
import android.os.Bundle
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.notification.NotificationHandlerService.NotificationEvent.CANCEL
import com.duckduckgo.app.notification.NotificationHandlerService.NotificationEvent.SURVEY_LAUNCH
import com.duckduckgo.app.notification.NotificationRegistrar
import com.duckduckgo.app.notification.db.NotificationDao
import timber.log.Timber

class SurveyNotification(
    private val context: Context,
    private val notificationDao: NotificationDao
) : SchedulableNotification {

    override val id = "com.duckduckgo.feedback.survey.pull"
    override val launchIntent = SURVEY_LAUNCH
    override val cancelIntent = CANCEL

    override suspend fun canShow(): Boolean {
        //TODO add condition that determines how many times it should have been used
        if (notificationDao.exists(id)) {
            Timber.v("Notification already scheduled")
            return false
        }
        return true
    }

    suspend fun scheduled() {
        notificationDao.insert(Notification(id))
    }

    override suspend fun buildSpecification(): NotificationSpec {
        return SurveySpecification(context)
    }
}

class SurveySpecification(context: Context) : NotificationSpec {
    override val channel = NotificationRegistrar.ChannelType.FEEDBACK
    override val systemId = NotificationRegistrar.NotificationId.Survey
    override val name = "Launch feedback survey"
    override val icon = R.drawable.notification_logo
    override val title: String = context.getString(R.string.surveyNotificationTitle)
    override val description: String = context.getString(R.string.surveyNotificationDescription)
    override val launchButton: String? = null
    override val closeButton: String? = null
    override val pixelSuffix = "spull"
    override val autoCancel = true
    override val bundle: Bundle = Bundle()
    override val color: Int = R.color.cornflowerBlue
}