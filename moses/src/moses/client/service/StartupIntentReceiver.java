package moses.client.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// TODO: Auto-generated Javadoc
/**
 * The Class StartupIntentReceiver.
 * 
 * @author Jaco Hofmann
 */
public class StartupIntentReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, MosesService.class);
		context.startService(serviceIntent);
	}

}
