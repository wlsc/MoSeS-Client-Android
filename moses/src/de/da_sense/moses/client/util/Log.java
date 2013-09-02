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
package de.da_sense.moses.client.util;

/**
 * Wraps the android.util.Log class to be able to deactivate certain logging
 * events e.g. for release. 
 * Use LEVEL to control the amount of messages you want to be displayed in log. 
 * 
 * @author Jaco Hofmann, Wladimir Schmidt
 * 
 */
public class Log {
	
	/**
	 * Set to one of the values from android.util.log.X where x is
	 * {DEBUG, INFO, ERROR, VERBOSE, WARN, ASSERT} to control the amount
	 * of messages displayed in the log.
	 */
	private static int LEVEL = android.util.Log.VERBOSE;

	/**
	 * Try to place a DEBUG message
	 * @param tag The tag of the message
	 * @param msg The message
	 */
	static public void d(String tag, String msg) {
		if (LEVEL <= android.util.Log.DEBUG)
			android.util.Log.d(tag, msg);
	}

	/**
	 * Try to place an INFO message
	 * @param tag The tag of the message
	 * @param msg The message
	 */
	static public void i(String tag, String msg) {
		if (LEVEL <= android.util.Log.INFO)
			android.util.Log.i(tag, msg);
	}

	/**
	 * Try to place an ERROR message
	 * @param tag The tag of the message
	 * @param msg The message
	 */
	static public void e(String tag, String msg) {
		if (LEVEL <= android.util.Log.ERROR)
			android.util.Log.e(tag, msg);
	}

	/**
	 * Try to place a VERBOSE message
	 * @param tag The tag of the message
	 * @param msg The message
	 */	
	static public void v(String tag, String msg) {
		if (LEVEL <= android.util.Log.VERBOSE)
			android.util.Log.v(tag, msg);
	}

	/**
	 * Try to place a WARN message
	 * @param tag The tag of the message
	 * @param msg The message
	 */
	static public void w(String tag, String msg) {
		if (LEVEL <= android.util.Log.WARN)
			android.util.Log.w(tag, msg);
	}

	/**
	 * Try to place a ASSERT message
	 * @param tag The tag of the message
	 * @param msg The message
	 */
	static public void wtf(String tag, String msg) {
		if (LEVEL <= android.util.Log.ASSERT)
			android.util.Log.wtf(tag, msg);
	}

	/**
	 * Try to place a DEBUG message with a Throwable 
	 * @param tag The tag of the message
	 * @param msg The message
	 * @param tr The Throwable
	 */
	static public void d(String tag, String msg, Throwable tr) {
		if (LEVEL <= android.util.Log.DEBUG)
			android.util.Log.d(tag, msg, tr);
	}

	/**
	 * Try to place an INFO message with a Throwable
	 * @param tag The tag of the message
	 * @param msg The message
	 * @param tr The Throwable
	 */
	static public void i(String tag, String msg, Throwable tr) {
		if (LEVEL <= android.util.Log.INFO)
			android.util.Log.i(tag, msg, tr);
	}

	/**
	 * Try to place an ERROR message with a Throwable
	 * @param tag The tag of the message
	 * @param msg The message
	 * @param tr The Throwable
	 */
	static public void e(String tag, String msg, Throwable tr) {
		if (LEVEL <= android.util.Log.ERROR)
			android.util.Log.e(tag, msg, tr);
	}

	/**
	 * Try to place a VERBOSE message with a Throwable
	 * @param tag The tag of the message
	 * @param msg The message
	 * @param tr The Throwable
	 */
	static public void v(String tag, String msg, Throwable tr) {
		if (LEVEL <= android.util.Log.VERBOSE)
			android.util.Log.v(tag, msg, tr);
	}

	/**
	 * Try to place a WARN message with a Throwable
	 * @param tag The tag of the message
	 * @param msg The message
	 * @param tr The Throwable
	 */
	static public void w(String tag, String msg, Throwable tr) {
		if (LEVEL <= android.util.Log.WARN)
			android.util.Log.w(tag, msg, tr);
	}

	/**
	 * Try to place an ASSERT message with a Throwable
	 * @param tag The tag of the message
	 * @param msg The message
	 * @param tr The Throwable
	 */
	static public void wtf(String tag, String msg, Throwable tr) {
		if (LEVEL <= android.util.Log.ASSERT)
			android.util.Log.wtf(tag, msg, tr);
	}
}
