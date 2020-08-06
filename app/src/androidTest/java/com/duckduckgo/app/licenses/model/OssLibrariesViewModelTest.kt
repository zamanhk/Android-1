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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.duckduckgo.app.CoroutineTestRule
import com.duckduckgo.app.InstantSchedulersRule
import com.duckduckgo.app.licenses.store.OssLibrariesLoader
import com.duckduckgo.app.statistics.pixels.Pixel
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class OssLibrariesViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = InstantSchedulersRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val commandCaptor = ArgumentCaptor.forClass(OssLibrariesViewModel.Command::class.java)
    private var mockCommandObserver: Observer<OssLibrariesViewModel.Command> = mock()
    private var mockViewStateObserver: Observer<OssLibrariesViewModel.ViewState> = mock()
    private val viewStateCaptor = ArgumentCaptor.forClass(OssLibrariesViewModel.ViewState::class.java)

    private var ossLibrariesLoader: OssLibrariesLoader = mock()
    private var mockPixel: Pixel = mock()

    private val testee: OssLibrariesViewModel by lazy {
        val model = OssLibrariesViewModel(ossLibrariesLoader, mockPixel, coroutineRule.testDispatcherProvider)
        model.viewState.observeForever(mockViewStateObserver)
        model.command.observeForever(mockCommandObserver)
        model
    }

    @Test
    fun whenLicensesAreLoadedThenViewStateIsUpdated() {
        val defaultViewState = OssLibrariesViewModel.ViewState(someLicenses())

        whenever(ossLibrariesLoader.loadLibraries()).thenReturn(someLicenses())

        testee.loadLibraries()

        Mockito.verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertEquals(defaultViewState, viewStateCaptor.value)
    }

    @Test
    fun whenUserTapsOnLinkThenOpenLinkCommandIssued() {
        val license = aLicense()
        testee.userRequestedToOpenLink(license)

        assertCommandIssued<OssLibrariesViewModel.Command.OpenLink> {
            assertEquals(license.link, this.url)
        }
    }

    @Test
    fun whenUserTapsOnLicenseThenOpenLinkCommandIssued() {
        val license = aLicense()
        testee.userRequestedToOpenLicense(license)

        assertCommandIssued<OssLibrariesViewModel.Command.OpenLink> {
            assertEquals(license.licenseLink, this.url)
        }
    }

    private fun someLicenses(): List<OssLibrary> {
        return listOf(
            OssLibrary("one name", "one license", "one link", "licenseLink"),
            OssLibrary
                ("second name", "second license", "second link", "licenseLink")
        )
    }

    private fun aLicense(): OssLibrary {
        return OssLibrary("library", "license", "link", "licenseLink")
    }

    private inline fun <reified T : OssLibrariesViewModel.Command> assertCommandIssued(instanceAssertions: T.() -> Unit = {}) {
        Mockito.verify(mockCommandObserver, atLeastOnce()).onChanged(commandCaptor.capture())
        val issuedCommand = commandCaptor.allValues.find { it is T }
        assertNotNull(issuedCommand)
        (issuedCommand as T).apply { instanceAssertions() }
    }
}
