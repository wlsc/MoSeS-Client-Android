package moses.client.service.helpers;

import android.os.Handler;
import moses.client.abstraction.PingSender;

/**
 * @author Jaco Hofmann
 * 
 */
public class KeepSessionAlive {
	private Handler mHandler = new Handler();
	private boolean stopPosting = false;
	private PingSender pinger;

	private final int pingTime = 60000; // Every minute

	private Runnable mKeepAliveTask = new Runnable() {

		@Override
		public void run() {
			pinger.sendPing();
			if (!stopPosting)
				mHandler.postDelayed(this, pingTime);
		}

	};

	public KeepSessionAlive(Executor e) {
		pinger = new PingSender(e);
	}

	public void keepAlive(boolean b) {
		if (b) {
			mHandler.removeCallbacks(mKeepAliveTask);
			mHandler.postDelayed(mKeepAliveTask, pingTime);
		} else {
			mHandler.removeCallbacks(mKeepAliveTask);
			stopPosting = true;
		}
	}
}
