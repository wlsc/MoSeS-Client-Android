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
package de.da_sense.moses.client.abstraction;

import java.util.Observable;

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
import de.da_sense.moses.client.util.Toaster;

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

	private Context mContext;
	private Exception exception;
	private State state;
	private String apkId;

	private String resultName;
	private String resultDescription;
	private String resultStartDate;
	private String resultEndDate;
	private String resultApkVersion;
	
	private String errorMessage;

	public boolean sendEvenWhenNoNetwork = false;
	private boolean cancelled = false;

	public ExternalApplicationInfoRetriever(String apkId, Context c) {
		this.mContext = c;
		this.apkId = apkId;
		setState(State.INITIALIZED);
	}

	public void cancel() {
		this.cancelled = true;
	}

	public void start() {
		if (!sendEvenWhenNoNetwork && !MosesService.isOnlineOrIsConnecting(mContext)) {
			setState(State.NO_NETWORK);
			return;
		} else {
			if (!MosesService.isOnlineOrIsConnecting(mContext)) {
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
									if(!MosesService.isOnline(mContext))
										Toaster.showNoInternetConnection(mContext);
									else
										Toaster.showBadServerResponseToast(mContext);
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
