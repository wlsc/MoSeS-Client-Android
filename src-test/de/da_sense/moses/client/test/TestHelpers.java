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