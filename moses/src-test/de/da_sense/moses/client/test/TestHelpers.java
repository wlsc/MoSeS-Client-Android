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

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;

public class TestHelpers {
	/**
	 * Must not be executed on the UI thread
	 * 
	 * @param x x-position in px on the screen
	 * @param y y-position in px on the screen
	 * @param instrumentation the instrumentation that is to be used
	 */
	public static void clickPositionOnScreen(float x, float y, Instrumentation instrumentation) {
		long begin = SystemClock.uptimeMillis();
		instrumentation.sendPointerSync(MotionEvent.obtain(begin, begin, MotionEvent.ACTION_DOWN, x, y, 0));
		myWait(20);
		instrumentation.sendPointerSync(MotionEvent.obtain(begin, begin+20, MotionEvent.ACTION_UP, x, y, 0));
		myWait(20);
	}
	
	public static void myWait(int t) {
		long t0 = System.currentTimeMillis();
		while (System.currentTimeMillis() - t0 < t)
			;
	}
	
	public static void myWaitRandom(int t) {
		double div = (double)t*0.2D;
		double divReal = Math.random()*2*div;
		myWait((int)Math.round(t-div+divReal));
	}
	
	public static boolean mystery() {
		return true;
	}
}
