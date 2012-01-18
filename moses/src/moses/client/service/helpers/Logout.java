package moses.client.service.helpers;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogout;
import moses.client.service.MosesService;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class Logout.
 *
 * @author Jaco Hofmann
 */
public class Logout {

	/**
	 * The Class LogoutFunc.
	 */
	private class LogoutFunc implements ReqTaskExecutor {

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * moses.client.com.ReqTaskExecutor#handleException(java.lang.Exception)
		 */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.LOGOUT", "FAILURE: " + e.getMessage());
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see moses.client.com.ReqTaskExecutor#postExecution(java.lang.String)
		 */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO: Handle unsuccessful logout
				if (j.getString("MESSAGE").equals("LOGOUT_RESPONSE")) {
					serv.loggedOut();
					e.execute();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * moses.client.com.ReqTaskExecutor#updateExecution(moses.client.com
		 * .NetworkJSON.BackgroundException)
		 */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				Toast.makeText(serv, c.c.toString(), Toast.LENGTH_LONG).show();
			} else {
				handleException(c.e);
			}
		}

	}

	/** The serv. */
	private MosesService serv;

	/** The e. */
	private Executor e;

	/**
	 * Instantiates a new logout.
	 *
	 * @param serv
	 *            the serv
	 * @param e
	 *            the e
	 */
	public Logout(MosesService serv, Executor e) {
		this.serv = serv;
		this.e = e;
		new RequestLogout(new LogoutFunc(), serv.getSessionID()).send();
	}
}
