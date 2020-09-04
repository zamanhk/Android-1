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

package com.duckduckgo.app.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.WebSettings
import com.duckduckgo.app.browser.BrowserChromeClient
import com.duckduckgo.app.browser.BrowserWebViewClient
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.global.DuckDuckGoActivity
import com.duckduckgo.app.global.view.show
import kotlinx.android.synthetic.main.include_duckduckgo_browser_webview.browserWebView
import kotlinx.android.synthetic.main.include_toolbar.toolbar
import javax.inject.Inject

class SearchSettingsActivity : DuckDuckGoActivity() {

    @Inject
    lateinit var webViewClient: BrowserWebViewClient

    @Inject
    lateinit var webChromeClient: BrowserChromeClient

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, SearchSettingsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_settings)
        setupToolbar(toolbar)

        configureWebView()

    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        browserWebView?.let {
            it.settings.apply {
                it.webViewClient = webViewClient
                it.webChromeClient = webChromeClient

                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                setSupportMultipleWindows(true)
                setSupportZoom(true)
            }
            it.show()
            it.loadUrl("https://duckduckgo.com/settings?q=ko=-1")
        }

    }

}