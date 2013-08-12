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
	 * This method shows a toast to the user stating that server is facing problems.
	 * 
	 * @param context the context of the activity
	 */
	public static void showBadServerResponseToast(Context context){
		showToast(context, context.getString(R.string.error_moses_server_bad_response));
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
	
}
