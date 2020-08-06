/*
 * Copyright (c) 2017 DuckDuckGo
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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.app.global.DefaultDispatcherProvider
import com.duckduckgo.app.global.DispatcherProvider
import com.duckduckgo.app.global.SingleLiveEvent
import com.duckduckgo.app.licenses.store.OssLicensesLoader
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelName.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class OssLicensesViewModel @Inject constructor(
    private val licensesLoader: OssLicensesLoader,
    private val pixel: Pixel,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    data class ViewState(
        val licenses: List<OssLicense> = emptyList()
    )

    sealed class Command {
        data class OpenLink(val url: String) : Command()
    }

    val viewState: MutableLiveData<ViewState> = MutableLiveData<ViewState>().apply {
        value = ViewState()
    }

    val command: SingleLiveEvent<Command> = SingleLiveEvent()

    init {
        pixel.fire(OPEN_SOURCE_LICENSES_OPENED)
    }

    fun loadLicenses() {
        viewModelScope.launch(dispatchers.main()) {
            val licenses = licensesLoader.loadLicenses()
            viewState.value = currentViewState().copy(licenses = licenses)
        }
    }

    fun userRequestedToOpenLink(license: OssLicense) {
        command.value = Command.OpenLink(license.link)
    }

    fun userRequestedToOpenLicense(license: OssLicense) {
        command.value = Command.OpenLink(license.licenseLink)
    }

    private fun currentViewState(): ViewState {
        return viewState.value!!
    }
}
