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

package com.duckduckgo.app.licenses.store

import android.content.Context
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.licenses.model.LibrariesJson
import com.duckduckgo.app.licenses.model.OssLibrary
import com.squareup.moshi.Moshi
import javax.inject.Inject

interface OssLibrariesLoader {
    fun loadLibraries(): List<OssLibrary>
}

class JsonOssLibrariesLoader @Inject constructor(
    private val context: Context,
    private val moshi: Moshi
) : OssLibrariesLoader {

    override fun loadLibraries(): List<OssLibrary> {
        val json = context.resources.openRawResource(R.raw.oss_libraries).bufferedReader().use { it.readText() }
        val adapter = moshi.adapter(LibrariesJson::class.java)
        val licenses = adapter.fromJson(json)
        return licenses?.jsonToLibraries() ?: emptyList()
    }
}
