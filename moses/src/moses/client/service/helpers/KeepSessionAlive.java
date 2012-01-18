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
	private final int pingTimeShort = 1000;

	private Runnable mKeepAliveTask = new Runnable() {

		@Override
		public void run() {
			pinger.sendPing();
			if (!stopPosting)
				if (bShortenPingTime) {
					mHandler.postDelayed(this, pingTimeShort);
				} else
					mHandler.postDelayed(this, pingTime);
		}

	};
	private boolean bShortenPingTime = false;
	
	public boolean isPingTimeShortened() {
		return bShortenPingTime;
	}

	public KeepSessionAlive(Executor e) {
		pinger = new PingSender(e);
	}

	public void shortenPingTime(boolean b) {
		this.bShortenPingTime = b;
		mHandler.removeCallbacks(mKeepAliveTask);
		mHandler.postDelayed(mKeepAliveTask, pingTimeShort);
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
