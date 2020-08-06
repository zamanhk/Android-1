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

package com.duckduckgo.app.licenses.model

class LibrariesJson {
    lateinit var licenses: List<LibrariesJsonModel>

    fun jsonToLibraries(): List<OssLibrary> {
        return licenses.mapNotNull {
            val name = it.name ?: return@mapNotNull null
            val license = it.license ?: return@mapNotNull null
            val link = it.link ?: return@mapNotNull null
            val licenseLink = it.licenseLink ?: return@mapNotNull null
            OssLibrary(name, license, link, licenseLink)
        }
    }
}

data class LibrariesJsonModel(
    val name: String,
    val license: String,
    val link: String,
    val licenseLink: String
)
