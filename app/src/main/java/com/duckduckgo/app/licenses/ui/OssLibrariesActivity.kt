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
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.global.DuckDuckGoActivity
import com.duckduckgo.app.licenses.model.OssLibrariesViewModel
import kotlinx.android.synthetic.main.content_oss_libraries.librariesList
import kotlinx.android.synthetic.main.include_toolbar.*

class OssLibrariesActivity : DuckDuckGoActivity() {

    private val viewModel: OssLibrariesViewModel by bindViewModel()
    private val librariesAdapter: OssLibrariesAdapter =
        OssLibrariesAdapter(onItemClick = { item -> viewModel.userRequestedToOpenLink(item) }, onLicenseLink = { item -> viewModel.userRequestedToOpenLicense(item) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oss_libraries)
        setupToolbar(toolbar)

        configureRecycler()
        observeViewModel()
    }

    private fun configureRecycler() {
        librariesList.layoutManager = LinearLayoutManager(this)
        librariesList.adapter = librariesAdapter
    }

    private fun observeViewModel() {
        viewModel.viewState.observe(this, Observer<OssLibrariesViewModel.ViewState> { viewState ->
            viewState?.let {
                librariesAdapter.notifyChanges(viewState.licens)
            }
        })

        viewModel.command.observe(this, Observer {
            processCommand(it)
        })

        viewModel.loadLibraries()
    }

    private fun processCommand(it: OssLibrariesViewModel.Command?) {
        when (it) {
            is OssLibrariesViewModel.Command.OpenLink -> openLink(it.url)
        }
    }

    private fun openLink(url: String) {
        startActivity(BrowserActivity.intent(this, url))
        finish()
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, OssLibrariesActivity::class.java)
        }
    }
}
