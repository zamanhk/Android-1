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
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteEntity
import com.duckduckgo.app.fire.fireproofwebsite.ui.FireproofWebsitesViewModel
import com.duckduckgo.app.licenses.store.OssLicensesLoader
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

class OssLicensesViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = InstantSchedulersRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val commandCaptor = ArgumentCaptor.forClass(OssLicensesViewModel.Command::class.java)
    private var mockCommandObserver: Observer<OssLicensesViewModel.Command> = mock()
    private var mockViewStateObserver: Observer<OssLicensesViewModel.ViewState> = mock()
    private val viewStateCaptor = ArgumentCaptor.forClass(OssLicensesViewModel.ViewState::class.java)

    private var licensesLoader: OssLicensesLoader = mock()
    private var mockPixel: Pixel = mock()

    private val testee: OssLicensesViewModel by lazy {
        val model = OssLicensesViewModel(licensesLoader, mockPixel, coroutineRule.testDispatcherProvider)
        model.viewState.observeForever(mockViewStateObserver)
        model.command.observeForever(mockCommandObserver)
        model
    }

    @Test
    fun whenLicensesAreLoadedThenViewStateIsUpdated() {
        val defaultViewState = OssLicensesViewModel.ViewState(someLicenses())

        whenever(licensesLoader.loadLicenses()).thenReturn(someLicenses())

        testee.loadLicenses()

        Mockito.verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertEquals(defaultViewState, viewStateCaptor.value)
    }

    @Test
    fun whenUserTapsOnLinkThenOpenLinkCommandIssued(){
        val license = aLicense()
        testee.userRequestedToOpenLink(license)

        assertCommandIssued<OssLicensesViewModel.Command.OpenLink> {
            assertEquals(license, this.license)
        }

    }

    private fun someLicenses(): List<OssLicense> {
        return listOf(
            OssLicense("one name", "one license", "one link"),
            OssLicense
                ("second name", "second license", "second link")
        )
    }

    private fun aLicense(): OssLicense {
        return OssLicense("library", "license", "link")
    }

    private inline fun <reified T : OssLicensesViewModel.Command> assertCommandIssued(instanceAssertions: T.() -> Unit = {}) {
        Mockito.verify(mockCommandObserver, atLeastOnce()).onChanged(commandCaptor.capture())
        val issuedCommand = commandCaptor.allValues.find { it is T }
        assertNotNull(issuedCommand)
        (issuedCommand as T).apply { instanceAssertions() }
    }
}