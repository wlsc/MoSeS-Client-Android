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

import java.util.Collections;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.userstudy.Form;
import de.da_sense.moses.client.userstudy.Survey;

/**
 * 
 * The {@link android.support.v4.view.PagerAdapter} that will provide {@link FormFragment}s representing
 * each {@link Form}.
 * 
 * @author Zijad Maksuti
 *
 */
class SurveyActivityPagerAdapter extends FragmentStatePagerAdapter{
	
	/**
	 * The ID of the {@link InstalledExternalApplication} instance containing the {@link Survey} with {@link Form}s
	 * which need to be visualized by the {@link FormFragment} instances returned by this {@link SurveyActivityPagerAdapter}.
	 */
	private String mAPKID;
	
	/**
	 * The list of {@link Form} instances of the survey.<p>
	 * <b>Note that this list is sorted,
	 * not like the one by calling {@link Survey#getForms()} although their elements are equivalent.</b>
	 */
	private List<Form> mForms;
	
	/**
	 * Constructs a new {@link SurveyActivityPagerAdapter} based on the consumed arguments.
	 * @param fm an instance of {@link FragmentManager}
	 * @param apk an {@link ExternalApplication} containing the {@link Form} instances for which this adapter instantiates
	 * {@link FormFragment}s.
	 */
	public SurveyActivityPagerAdapter(FragmentManager fm, ExternalApplication apk) {
		super(fm);
		this.mAPKID = apk.getID();
		this.mForms = apk.getSurvey().getForms();
		Collections.sort(mForms); // sort the forms
    }

    @Override
    public Fragment getItem(int i) {
        FormFragment formFragment = new FormFragment();
        formFragment.setPosition(i);
        formFragment.setFormID(mForms.get(i).getId());
        formFragment.setAPKID(mAPKID);
        
        if(i==0)
        	formFragment.setIsFirst(true);
        else
        	formFragment.setIsFirst(false);
        
        if(i==getCount()-1)
        	formFragment.setIsLast(true);
        else
        	formFragment.setIsLast(false);
        
        return formFragment;
    }

    @Override
    public int getCount() {
        return mForms.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mForms.get(position).getTitle();
    }
	
}
