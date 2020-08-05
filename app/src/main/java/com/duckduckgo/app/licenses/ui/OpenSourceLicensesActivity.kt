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

package com.duckduckgo.app.licenses.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.global.DuckDuckGoActivity
import com.duckduckgo.app.licenses.model.OssLicense
import com.duckduckgo.app.licenses.model.OssLicensesViewModel
import kotlinx.android.synthetic.main.content_oss_licenses.licensesList
import kotlinx.android.synthetic.main.include_toolbar.*

class OpenSourceLicensesActivity : DuckDuckGoActivity() {

    private val viewModel: OssLicensesViewModel by bindViewModel()
    private val licensesAdapter: OpenSourceLicensesAdapter = OpenSourceLicensesAdapter { license ->
        viewModel.userRequestedToOpenLink(license)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oss_licenses)
        setupToolbar(toolbar)

        configureRecycler()
        observeViewModel()
    }

    private fun configureRecycler() {
        licensesList.layoutManager = LinearLayoutManager(this)
        licensesList.adapter = licensesAdapter
    }

    private fun observeViewModel(){
        viewModel.viewState.observe(this, Observer<OssLicensesViewModel.ViewState> { viewState ->
            viewState?.let {
                licensesAdapter.notifyChanges(viewState.licenses)
            }
        })

        viewModel.command.observe(this, Observer {
            processCommand(it)
        })

        viewModel.loadLicenses()
    }

    private fun processCommand(it: OssLicensesViewModel.Command?) {
        when (it) {
            is OssLicensesViewModel.Command.OpenLink -> openLink(it.license)
        }
    }

    private fun openLink(license: OssLicense) {

    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, OpenSourceLicensesActivity::class.java)
        }
    }

}
