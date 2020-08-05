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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.licenses.model.OssLicense
import kotlinx.android.synthetic.main.item_oss_license.view.oss_license
import kotlinx.android.synthetic.main.item_oss_license.view.oss_name

class OssLicensesAdapter(private val onClick: (OssLicense) -> Unit) : RecyclerView.Adapter<OssLicensesAdapter.LicenseViewHolder>() {

    private var licensesViewData: MutableList<OssLicense> = mutableListOf()

    class LicenseViewHolder(
        val root: View,
        val name: TextView,
        val license: TextView
    ) : RecyclerView.ViewHolder(root)

    override fun getItemCount() = licensesViewData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.item_oss_license, parent, false)
        return LicenseViewHolder(root, root.oss_name, root.oss_license)
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        val viewElement = licensesViewData[position]
        holder.itemView.setOnClickListener { onClick.invoke(viewElement) }
        holder.name.text = viewElement.name
        holder.license.text = viewElement.license
    }

    fun notifyChanges(newList: List<OssLicense>) {
        licensesViewData = newList.toMutableList()
        notifyDataSetChanged()
    }
}
