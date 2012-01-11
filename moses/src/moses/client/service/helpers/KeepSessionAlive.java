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
	
	private final int pingTime = 10000;

	private Runnable mKeepAliveTask = new Runnable() {

		@Override
		public void run() {
			pinger.sendPing();
			if (!stopPosting)
				mHandler.postDelayed(this, pingTime);
		}

	};

	public KeepSessionAlive() {
		pinger = new PingSender(new Executor() {
		@Override
		public void execute() {
			// TODO: Message handling
		}});
	}

	public void keepAlive(boolean b) {
		if (b) {
			mHandler.removeCallbacks(mKeepAliveTask);
			mHandler.postDelayed(mKeepAliveTask, pingTime);
		} else {
			stopPosting = true;
		}
	}
}
