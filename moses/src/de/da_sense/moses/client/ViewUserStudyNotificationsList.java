package de.da_sense.moses.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import de.da_sense.moses.client.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.ExternalApplicationInfoRetriever;
import de.da_sense.moses.client.abstraction.ExternalApplicationInfoRetriever.State;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.userstudy.UserStudyNotification;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;

/**
 * Viewing and installing apks from the server
 * 
 * @author Simon L
 */
public class ViewUserStudyNotificationsList extends ListActivity {

	private static final int REFRESH_REDUNDANT_TRESHOLD = 1000;
	private final static long EXPIRATION_TIME_USERSTUDIES = 1L * 60L * 1000L;
	private static final int showStudyRequestcode = 5;
	private ListView listView;
	private List<UserStudyNotification> userStudies;
	private UserStudyNotification lastStartedDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pendinguserstudieslist);

		initControls();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == showStudyRequestcode) {
			UserStudyNotification app = lastStartedDialog;
			if (resultCode == Activity.RESULT_OK) {

			} else {

			}

			drawUserStudies();
		}
	}

	public void studyOnClickHandler(View v) {
		int pos = listView.getPositionForView(v);
		final UserStudyNotification app = userStudies.get(pos);

		Intent intent = new Intent(MosesService.getInstance(), ViewUserStudyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ViewUserStudyActivity.EXTRA_USER_STUDY_APK_ID, app.getApplication().getID());

		// TODO: return code management
		lastStartedDialog = app;
		startActivityForResult(intent, showStudyRequestcode);

	}

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		refresherCalls();
	}

	private void drawUserStudies() {
		populateList(userStudies);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		refresherCalls(true);
	}

	private void refresherCalls() {
		refresherCalls(false);
	}

	private long lastRefresherCallsTime = System.currentTimeMillis() - 100000000;
	private boolean isPaused;

	private void refresherCalls(boolean maybeRedundantSource) {
		boolean doIt = true;
		if (maybeRedundantSource && System.currentTimeMillis() - lastRefresherCallsTime < REFRESH_REDUNDANT_TRESHOLD) {
			doIt = false;
		}
		if (doIt) {
			retrieveStudies();
			removeExpiredUserStudies();
			drawUserStudies();
			scheduleRetrieveMissingInfos();
			lastRefresherCallsTime = System.currentTimeMillis();
		}
	}

	private void retrieveStudies() {
		if (UserstudyNotificationManager.getInstance() == null)
			UserstudyNotificationManager.init(this);
		List<UserStudyNotification> studies = UserstudyNotificationManager.getInstance().getNotifications();
		this.userStudies = studies;
	}

	private void removeExpiredUserStudies() {
		boolean hasRemoved = false;
		for (UserStudyNotification us : userStudies) {
			if (isExpired(us)) {
				UserstudyNotificationManager.getInstance().removeNotificationById(us.getApplication().getID());
				hasRemoved = true;
			}
		}
		if (hasRemoved) {
			try {
				UserstudyNotificationManager.getInstance().saveToDisk(this);
			} catch (IOException e) {
				Log.e("MoSeS.USERSTUDY", "Could not save expiration of user study because of IO Exception: ", e);
			}
			retrieveStudies();
		}
	}

	private boolean isExpired(UserStudyNotification us) {
		if (System.currentTimeMillis() - us.getDate().getTime() > EXPIRATION_TIME_USERSTUDIES) {
			return true;
		}
		return false;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.isPaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.isPaused = false;
		refresherCalls(true);
	}

	Map<String, ExternalApplicationInfoRetriever.State> retrieveCancels = new HashMap<String, ExternalApplicationInfoRetriever.State>();

	private void scheduleRetrieveMissingInfos() {
		retrieveCancels.clear();
		for (UserStudyNotification n : UserstudyNotificationManager.getInstance().getNotifications()) {
			if (!isNotificationToDisplay(n)) {
				scheduleRetrieveMissingInfo(n);
			}
		}
	}

	private void scheduleRetrieveMissingInfo(final UserStudyNotification n) {
		final ExternalApplicationInfoRetriever retriever = new ExternalApplicationInfoRetriever(n.getApplication()
				.getID(), this);
		retriever.sendEvenWhenNoNetwork = false;
		retriever.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (retriever.getState() == State.DONE) {
					// TODO:
					n.getApplication().setName(retriever.getResultName());
					n.getApplication().setDescription(retriever.getResultDescription());
					n.getApplication().setSensors(retriever.getResultSensors());
					UserstudyNotificationManager.getInstance().updateNotification(n);
					try {
						UserstudyNotificationManager.getInstance().saveToDisk(ViewUserStudyNotificationsList.this);
					} catch (IOException e) {
						Log.w("MoSeS.APK", "couldnt save manager: ", e);
					}
					if (!isPaused && !isFinishing()) {
						drawUserStudies();
					}
				}
				if (retriever.getState() == State.ERROR) {
					Log.e("MoSeS.USERSTUDY",
							"Wanted to display user study, but couldn't get app informations because of: ",
							retriever.getException());
					retrieveCancels.put(n.getApplication().getID(), State.ERROR);
					drawUserStudies();
				}
				if (retriever.getState() == State.NO_NETWORK) {
					retrieveCancels.put(n.getApplication().getID(), State.NO_NETWORK);
					drawUserStudies();
				}
			}
		});
		retriever.start();
	}

	private void populateList(List<UserStudyNotification> applications) {
		listView = getListView();

		TextView instructionsView = (TextView) findViewById(R.id.userstudyListHeaderInstructions);
		if (instructionsView != null) {
			// TODO: check if there is no notification WITH DATA (or just load
			// all descriptions/names in this activity, too)
			String staticStr = "A user study is an app which has been released to a limited number of devices for testing.";
			if (applications.size() == 0) {
				instructionsView.setText(staticStr + "\nNo user studies available.");
			} else {
				instructionsView.setText(staticStr + "\nClick on a user study to see the details.");
			}
		}

		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		for (UserStudyNotification app : applications) {
			if (isNotificationToDisplay(app)) {
				HashMap<String, String> rowMap = new HashMap<String, String>();
				rowMap.put("name", app.getApplication().getName());
				rowMap.put("description", app.getApplication().getDescription());
				listContent.add(rowMap);
			} else {
				HashMap<String, String> rowMap = new HashMap<String, String>();
				String nameLbl = app.getApplication().isNameSet() ? app.getApplication().getName()
						: (retrieveCancels.containsKey(app.getApplication().getID()) ? (retrieveCancels.get(app
								.getApplication().getID()) == State.NO_NETWORK ? "User study "
								+ app.getApplication().getID() + " (Could not load name: no network)"
								: "Description of user study " + app.getApplication().getID()
										+ " (Error at loading name)") : "Loading name...");
				String descriptionLbl = app.getApplication().isDescriptionSet() ? app.getApplication().getDescription()
						: (retrieveCancels.containsKey(app.getApplication().getID()) ? (retrieveCancels.get(app
								.getApplication().getID()) == State.NO_NETWORK ? "(Could not load description: no network)"
								: "(Could not load description)")
								: "Loading description...");
				rowMap.put("name", nameLbl);
				rowMap.put("description", descriptionLbl);
				listContent.add(rowMap);
			}
		}
		SimpleAdapter contentAdapter = new SimpleAdapter(this, listContent, R.layout.availablestudieslistitem,
				new String[] { "name", "description" }, new int[] { R.id.userstudylistitemtext,
						R.id.userstudylistitemdescription });

		listView.setAdapter(contentAdapter);
	}

	private static boolean isNotificationToDisplay(UserStudyNotification app) {
		return (app.getApplication().isNameSet()) && (app.getApplication().isNameSet());
	}

	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
