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

import android.content.Context;
import android.widget.Toast;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.service.MosesService;

/**
 * This class offers methods for toasting.
 * 
 * @author Zijad Maksuti
 *
 */
public class Toaster {
	
	/**
	 * This method shows a toast to the user.
	 * 
	 * @param context the context of the activity
	 * @param message the message to show
	 */
	public static void showToast(Context context, String message){
		
		Toast theToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		theToast.show();
	}
	
	/**
	 * This method shows a toast to the user.
	 * 
	 * @param context the context of the activity
	 * @param message the message to show
	 */
	public static void showToastLong(Context context, String message){
		
		Toast theToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		theToast.show();
	}
	
	/**
	 * This method shows a toast to the user stating that server is facing problems.
	 * 
	 * @param context the context of the activity
	 */
	public static void showBadServerResponseToast(Context context){
		showToast(context, context.getString(R.string.error_moses_server_bad_response));
	}
	
	/**
	 * This method shows a toast to the user stating that there is no internet connection.
	 * 
	 * @param context the context of the activity
	 */
	public static void showNoInternetConnection(Context context){
		showToast(context, context.getString(R.string.notification_no_internet_connection));
	}
	
	/**
	 * This method shows a toast to the user.<p>
	 * <b>Note</b>: the toast will only show if {@link MosesService} is running. If you can provide an
	 * instance of {@link Context}, use {@link Toaster#showToast(Context, String)} instead.
	 * 
	 * @param context the context of the activity
	 * @param message the message to show
	 */
	public static void showToast(String message){
		MosesService ms = MosesService.getInstance();
		Toast theToast;
		if(ms != null){
			theToast = Toast.makeText(ms, message, Toast.LENGTH_SHORT);
			theToast.show();
			}
	}
	
	/**
	 * This method shows a toast to the user.<p>
	 * <b>Note</b>: the toast will only show if {@link MosesService} is running. If you can provide an
	 * instance of {@link Context}, use {@link Toaster#showToast(Context, String)} instead.
	 * 
	 * @param context the context of the activity
	 * @param stringResource the string resource containing the message
	 */
	public static void showToast(int stringResource){
		MosesService ms = MosesService.getInstance();
		Toast theToast;
		if(ms != null){
			theToast = Toast.makeText(ms, ms.getString(stringResource), Toast.LENGTH_SHORT);
			theToast.show();
			}
	}
	
	/**
	 * This method shows a toast to the user stating that server is facing problems.<p>
	 * <b>Note</b>: the toast will only show if {@link MosesService} is running. If you can provide an
	 * instance of {@link Context}, use {@link Toaster#showBadServerResponseToast(Context)} instead.
	 * 
	 * @param context the context of the activity
	 */
	public static void showBadServerResponseToast(){
		MosesService ms = MosesService.getInstance();
		Toast theToast;
		if(ms != null){
			String message = ms.getString(R.string.error_moses_server_bad_response);
			theToast = Toast.makeText(ms, message, Toast.LENGTH_SHORT);
			theToast.show();
			}
	}
	
	/**
	 * This method shows a toast to the user stating that the client is offline.<p>
	 * <b>Note</b>: the toast will only show if {@link MosesService} is running. If you can provide an
	 * instance of {@link Context}, use {@link Toaster#showNoInternetConnection(Context)} instead.
	 * 
	 * @param context the context of the activity
	 */
	public static void showNoInternetConnectionToast(){
		MosesService ms = MosesService.getInstance();
		Toast theToast;
		if(ms != null){
			String message = ms.getString(R.string.notification_no_internet_connection);
			theToast = Toast.makeText(ms, message, Toast.LENGTH_SHORT);
			theToast.show();
			}
	}
	
}
