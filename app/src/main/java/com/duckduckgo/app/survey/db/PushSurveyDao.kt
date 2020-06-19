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

package com.duckduckgo.app.survey.db

import androidx.room.*

@Dao
interface PushSurveyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(survey: PushSurvey)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(survey: PushSurvey)

    @Query("select count(1) > 0 from push_survey where surveyId = 1")
    abstract fun exists(): Boolean

    @Query("select * from push_survey where surveyId = 1")
    abstract fun get(): PushSurvey?
}

@Entity(
    tableName = "push_survey"
)
data class PushSurvey(
    @PrimaryKey val surveyId: Int = 1,
    val q1: String,
    val q2: String,
    var sentCount: Int = 0,
    val lastSent: Long
)