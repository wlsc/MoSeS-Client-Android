/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.da_sense.moses.client;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import de.da_sense.moses.client.util.Log;

/**
 * Adapter whose getView method return a View with an onClickListener.
 * We use this Adapter for the lists in the Fragments.
 * @author Sandra Amend
 *
 */
public class MosesListAdapter extends SimpleAdapter {

	/**
	 * Constructor for a MosesListAdapter
	 * @param context the Activity
	 * @param data the data for the list adapter
	 * @param resource the resource layout file
	 * @param from array of the identifiers for the data 
	 * @param to array of the resource ids in which to display the data
	 */
	public MosesListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
	}

	/**
	 * Adds an onClickListener to the View before it gets returned.
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		View listElement = view.findViewById(R.id.apklistitemelement);
		
		
		listElement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("MosesListAdapter", "onClick on item " + position);
				WelcomeActivity mA = WelcomeActivity.getInstance();
				
				switch (mA.getActiveTab()) {
				case WelcomeActivityPagerAdapter.TAB_AVAILABLE: {
					Log.d("MosesListAdapter", "setting on click listener with available tab.");
					final AvailableFragment aF = AvailableFragment.getInstance();
					aF.showDetails(aF.getListIndexElement(position), mA, new Runnable() {
						@Override
						public void run() {
							Log.d("MosesListAdapter", "onClick on list position: " + position + " with ExternalApp position: " + aF.getListIndexElement(position));
							aF.handleInstallApp(aF.getExternalApps().get(aF.getListIndexElement(position)));
						}
					}, new Runnable() {
						@Override
						public void run() {
						}
					});
					break;
				}
				case WelcomeActivityPagerAdapter.TAB_RUNNING: {
					Log.d("MosesListAdapter", "setting on click listener with running tab.");
					final RunningFragment rF = RunningFragment.getInstance();
					rF.showDetails(position, mA, new Runnable() {
						@Override
						public void run() {
							rF.handleStartApp(rF.getInstalledApps().get(position));
						}
					}, new Runnable() {
						@Override
						public void run() {
						}
					});
					break;
				}
				case WelcomeActivityPagerAdapter.TAB_HISTORY:
					Log.d("MosesListAdapter", "setting on click listener with history tab.");
					final HistoryFragment hF = HistoryFragment.getInstance();
					hF.showDetails(position, mA, new Runnable() {
						@Override
						public void run() {
						}
					}, new Runnable() {
						@Override
						public void run() {
						}
					});
					break;
				default: throw new IllegalStateException("No valid tab selection!");
			}
			}
		});
		return view;
	}
}
