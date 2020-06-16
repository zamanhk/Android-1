/*
 * Copyright (c) 2018 DuckDuckGo
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

package com.duckduckgo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.global.DuckDuckGoApplication
import com.duckduckgo.app.global.install.AppInstallStore
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelName.WIDGETS_ADDED
import com.duckduckgo.app.statistics.pixels.Pixel.PixelName.WIDGETS_DELETED
import com.duckduckgo.app.systemsearch.SystemSearchActivity
import com.duckduckgo.app.widget.ui.AppWidgetCapabilities
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Inject

class SearchWidgetLight : SearchWidget(R.layout.search_widget_light)

@EntryPoint
@InstallIn(ApplicationComponent::class)
interface SearchWidgetEntryPoint {
    fun appInstallStore(): AppInstallStore
    fun pixel(): Pixel
    fun widgetCapabilities(): AppWidgetCapabilities
}

open class SearchWidget(val layoutId: Int = R.layout.search_widget) : AppWidgetProvider() {

    lateinit var appInstallStore: AppInstallStore
    lateinit var pixel: Pixel
    lateinit var widgetCapabilities: AppWidgetCapabilities

    override fun onReceive(context: Context, intent: Intent?) {
        inject(context)
        super.onReceive(context, intent)
    }

    private fun inject(context: Context) {
        val appContext = context?.applicationContext ?: throw IllegalStateException()
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, SearchWidgetEntryPoint::class.java)

        appInstallStore = hiltEntryPoint.appInstallStore()
        pixel = hiltEntryPoint.pixel()
        widgetCapabilities = hiltEntryPoint.widgetCapabilities()
    }

    override fun onEnabled(context: Context) {
        if (!appInstallStore.widgetInstalled) {
            appInstallStore.widgetInstalled = true
            pixel.fire(WIDGETS_ADDED)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, layoutId)
        views.setOnClickPendingIntent(R.id.widgetContainer, buildPendingIntent(context))
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = SystemSearchActivity.fromWidget(context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray?) {
        if (appInstallStore.widgetInstalled && !widgetCapabilities.hasInstalledWidgets) {
            appInstallStore.widgetInstalled = false
            pixel.fire(WIDGETS_DELETED)
        }
    }
}
