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
package de.da_sense.moses.client.test;

import org.json.JSONObject;

import de.da_sense.moses.client.MosesActivity;
import de.da_sense.moses.client.ViewUserStudyActivity;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.EHookTypes;
import de.da_sense.moses.client.service.helpers.UserStudyStatusBarHelper;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
import de.da_sense.moses.client.userstudy.UserStudyNotification.Status;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import de.da_sense.moses.client.util.Log;
import static moses.client.test.TestHelpers.*;

public class HookTests extends ActivityInstrumentationTestCase2<MosesActivity> {

	private TestResponseGenerator r = null;
	
	public HookTests() {
		super(MosesActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		NetworkJSON.debug = true;
		NetworkJSON.threadProblem = true;
		r = new TestResponseGenerator();
		NetworkJSON.response = r;
		SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("username_pref", "alex");
		e.putString("password_pref", "777");
		e.putString("deviceid_pref", "someid");
		e.putBoolean("splashscreen_pref", false);
		e.putBoolean("firststart", false);
		e.commit();
		
	}

	public void testLoginHooks() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(400);
		if(mystery()||false) return;
	}
	
	public void testLoginFirstHooks() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(1500);
		if(mystery()||false) return;
	}
	
	public void testOnTimeHooks() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(800);
		if(mystery()||false) return;
		
		assertNotNull(MosesService.getInstance());
		
		MosesService.getInstance().reloadSettings();
		
		//Test various hook queues to be not null
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINEND));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINFAILED));

		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSTART));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSUCCESS));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSUCCESSPRIORITY));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSUCCESS));
		
		//At last, MoseS service must be logged in =)
		assertTrue(MosesService.getInstance().isLoggedIn());
	}
	
}
