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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.global.DuckDuckGoActivity
import com.duckduckgo.app.survey.ui.PushSurveyViewModel.Command
import com.duckduckgo.app.survey.ui.PushSurveyViewModel.Command.Close
import kotlinx.android.synthetic.main.activity_push_survey.*

class PushSurveyActivity : DuckDuckGoActivity() {

    private val viewModel: PushSurveyViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_survey)
        configureListeners()

        configureObservers()

        val lastCommand = viewModel.command.value
        if (lastCommand != null) {
            processCommand(lastCommand)
        }
    }

    private fun configureListeners() {
        submitButton.setOnClickListener {
            viewModel.onSubmitPressed()
        }
        dismissButton.setOnClickListener {
            viewModel.onSurveyDismissed()
        }
    }

    private fun configureObservers() {
        viewModel.command.observe(this, Observer {
            it?.let { command -> processCommand(command) }
        })
    }

    private fun processCommand(command: Command) {
        when (command) {
            is Command.EnableSubmission -> submitButton.isEnabled = true
            is Command.ShowSuccessMessage -> showSuccessMessage()
            is Close -> finish()
        }
    }

    fun onQ1Answered(view: View) {
        val answer = view.tag as String
        viewModel.onQ1Answered(answer)
    }

    fun onQ2Answered(view: View) {
        val answer = view.tag as String
        viewModel.onQ2Answered(answer)
    }

    private fun showSuccessMessage() {
        Toast.makeText(this, getString(R.string.surveySubmissionMessage), Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        viewModel.onSurveyDismissed()
    }

    companion object {

        fun intent(context: Context): Intent {
            return Intent(context, PushSurveyActivity::class.java)
        }

        const val SURVEY_EXTRA = "SURVEY_EXTRA"
    }
}
