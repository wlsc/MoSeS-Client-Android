package de.da_sense.moses.client.abstraction;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestGetApkInfo;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.Executable;
import de.da_sense.moses.client.service.helpers.HookTypesEnum;
import de.da_sense.moses.client.service.helpers.MessageTypesEnum;
import de.da_sense.moses.client.util.Log;

public class ExternalApplicationInfoRetriever extends Observable {

	public static enum State {
		/**
		 * not even sent
		 */
		INITIALIZED,
		/**
		 * terminal state, success! retrieve results via getResultName) etc.
		 */
		DONE,
		/**
		 * Request is pending
		 */
		PENDING,
		/**
		 * non-terminal state, can only occur when the flag
		 * {@link ExternalApplicationInfoRetriever#sendEvenWhenNoNetwork} is
		 * true;
		 */
		NO_NETWORK_PENDING,
		/**
		 * terminal state, can only occur when the flag
		 * {@link ExternalApplicationInfoRetriever#sendEvenWhenNoNetwork} is
		 * false;
		 */
		NO_NETWORK,
		/**
		 * An Error occured; retrieve the exception via
		 * {@link ExternalApplicationInfoRetriever#getException()}. (The error
		 * is always signalized via exceptions, so some of them will be generic
		 * RuntimeExceptions with Messages indicating what happened).
		 */
		ERROR
	}

	private Context context;
	private Exception exception;
	private State state;
	private String apkId;

	private String resultName;
	private String resultDescription;
	private List<Integer> resultSensors;
	private String resultStartDate;
	private String resultEndDate;
	private String resultApkVersion;
	
	private String errorMessage;

	public boolean sendEvenWhenNoNetwork = false;
	private boolean cancelled = false;

	public ExternalApplicationInfoRetriever(String apkId, Context c) {
		this.context = c;
		this.apkId = apkId;
		setState(State.INITIALIZED);
	}

	public void cancel() {
		this.cancelled = true;
	}

	public void start() {
		if (!sendEvenWhenNoNetwork && !MosesService.isOnline(context)) {
			setState(State.NO_NETWORK);
			return;
		} else {
			if (!MosesService.isOnline(context)) {
				setState(State.NO_NETWORK_PENDING);
			} else {
				setState(State.PENDING);
			}
			if (MosesService.getInstance() != null) {
				Executable executor = new Executable() {

					@Override
					public void execute() {
						final RequestGetApkInfo r = new RequestGetApkInfo(new ReqTaskExecutor() {

							@Override
							public void updateExecution(BackgroundException c) {
							}

							@Override
							public void postExecution(String s) {
								try {
									Log.d("MoSeS.APK", "getApkInfo Answer received: " + s);
									JSONObject j = new JSONObject(s);
									if (RequestGetApkInfo.isInfoRetrieved(j)) {
										resultName = j.getString("NAME");
										resultDescription = j.getString("DESCR");
										JSONArray sensorsArray = j.getJSONArray("SENSORS");
										resultSensors = new LinkedList<Integer>();
										for (int i = 0; i < sensorsArray.length(); i++) {
											resultSensors.add(sensorsArray.getInt(i));
										}
										resultStartDate = j.getString("STARTDATE");
										resultEndDate = j.getString("ENDDATE");
										resultApkVersion = j.getString("APKVERSION");
										
										setState(State.DONE);
									} else {
										Log.e("MoSeS.APK", "apk info request: Server returned negative" + j.toString());
										setErrorState(new RuntimeException(
												"APKINFOREQUEST: answer from server seems to be negative: " + s));
									}
								} catch (JSONException e) {
									Log.e("MoSeS.APK", "requesting study information: json exception" + e.getMessage());
									setErrorState(e);
								}
							}

							@Override
							public void handleException(Exception e) {
								Log.e("MoSeS.APK", "couldn't load apk information" + e.getMessage(), e);
								setErrorState(e);
							}
						}, apkId, MosesService.getInstance().getSessionID());

						Log.d("MoSeS.APK", "sending GETAPKINFO request");
						r.send();
					}
				};
				MosesService.getInstance().executeLoggedIn(HookTypesEnum.POST_LOGIN_SUCCESS,
						MessageTypesEnum.REQUEST_GET_APK_INFO, executor);
			} else {
				setErrorState(new RuntimeException("cannot send GETAPKINFO request because the Service is not present."));
			}
		}
	}

	private void setState(State state) {
		if (!cancelled) {
			this.state = state;
			setChanged();
			notifyObservers();
		}
	}

	private void setErrorState(Exception e) {
		this.exception = e;
		this.errorMessage = e.getMessage();
		this.setState(State.ERROR);
	}

	public Exception getException() {
		return exception;
	}

	public State getState() {
		return state;
	}

	public String getResultName() {
		return resultName;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public List<Integer> getResultSensors() {
		return resultSensors;
	}

    public String getResultStartDate() {
        return resultStartDate;
    }

    public String getResultEndDate() {
        return resultEndDate;
    }

    public String getResultApkVersion() {
        return resultApkVersion;
    }

}
