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

import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.httpsupgrade.api.HttpsWhitelistJsonAdapter
import com.duckduckgo.app.httpsupgrade.model.HttpsWhitelistedDomain
import com.duckduckgo.app.licenses.model.LicensesJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.*
import org.junit.Test

class JsonOssLicensesLoaderTest {

    @Test
    fun whenJsonFileIsReadThenLicensesAreValid() {
        val jsonLicenses = InstrumentationRegistry.getInstrumentation().targetContext.resources.openRawResource(R.raw.oss_licenses)
            .bufferedReader().use { it.readText() }

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(LicensesJson::class.java)
        val root = adapter.fromJson(jsonLicenses)!!

        assertEquals(37, root.licenses.size)

        val firstLicense = root.licenses.first()
        assertEquals(firstLicense.name, "Android Support Libraries")
        assertEquals(firstLicense.license, "Apache 2.0")
        assertEquals(firstLicense.link, "https://android.googlesource.com/platform/frameworks/support/")
        assertEquals(firstLicense.licenseLink, "https://apache.org/licenses/LICENSE-2.0")
    }
}