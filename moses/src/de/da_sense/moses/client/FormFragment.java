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
package de.da_sense.moses.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplication;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestSendSurveyAnswers;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.userstudy.Form;
import de.da_sense.moses.client.userstudy.PossibleAnswer;
import de.da_sense.moses.client.userstudy.Question;
import de.da_sense.moses.client.userstudy.Survey;
import de.da_sense.moses.client.util.Log;
import de.da_sense.moses.client.util.Toaster;

/**
 * Instance of this class represent a {@link Fragment} which visualizes the an instance of a
 * {@link Form}. The form is passed to by its ID. See {@link #setFormID(int)}.
 * 
 * @author Ibrahim Alyahya, Sandra Amend, Florian Schnell, 
 * @author Wladimir Schmidt
 * @author Zijad Maksuti
 */
public class FormFragment extends Fragment {
	/**
	 * Defining a log tag to this class
	 */
	private static final String LOG_TAG = FormFragment.class.getName();

	/**
	 * The {@link Form} instance visualized with this {@link FormFragment}.
	 */
	private Form mForm;
	
	/**
	 * The APKID
	 */
	private String mAPKID = null;
	
	/**
	 * The id of the {@link Form} instance visualized by this {@link FormFragment}.
	 */
	private int mFormID = -1;
	
	private LayoutInflater mLayoutInflater;
	
	/**
	 * Set to true only if this fragment is on the first position in the {@link SurveyActivity}
	 */
	private Boolean mIsFirst = null;
	
	private static final String KEY_IS_FIRST = "keymIsFirst";
	
	/**
	 * Set to true only if this fragment is on the last position in the {@link SurveyActivity}
	 */
	private Boolean mIsLast = null;
	
	private static final String KEY_IS_LAST = "keymIsLast";
	
	/**
	 * The root {@link View} of this {@link Fragment}.
	 */
	private LinearLayout mRoot;
	
	private int mNumberOfAttemptsToSendResultsToServer = 0;
	
	/**
	 * Mappings between free questions and their edit texts. This mapping is needed in order to
	 * save the entered text in edit text to {@link Question} instance, BEFORE the results are sent to server.
	 */
	private Map<Question, EditText> mQuestionEditTextMappings = new HashMap<Question, EditText>();
	
	/**
	 * Mappings between {@link Question} represented by this {@link FormFragment} and EditText
	 * instances which hold their title representations.
	 */
	private Map<Question, TextView> mQuestionTitleMappings = new HashMap<Question, TextView>();
	
	/**
	 * The scroll view containing question in this fragment.
	 */
	private ScrollView mScrollView;
	
	/**
	 * The position of this Fragment in the view pager
	 */
	private int mPosition = -1;
	
	private static final String KEY_POSITION = "keyMPosition";
	
	/*
	 * Used to save position and question id of the fragment which contains a mandatory question which is unanswered
	 */
	private static final String KEY_POSITION_OF_FRAGMENT_WHICH_SHOULD_SCROLL = "keyPositionOfFragmentWhichShouldScroll";
	private static final String KEY_QUESTION_TO_SCROLL_TO = "keyQuestionToScrollTo";
	
	/*
	 * Signals to whom does the apk, whose Survey is being represented, belongs to
	 */
	private int mBelongsTo = WelcomeActivityPagerAdapter.TAB_RUNNING;
	

	/**
	 * Creates a Layout for a Single_Questionnaire
	 * 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Intent intent = getActivity().getIntent();
		mBelongsTo = intent.getIntExtra(WelcomeActivity.KEY_BELONGS_TO, WelcomeActivityPagerAdapter.TAB_RUNNING);
		
		mLayoutInflater = inflater;
	
		container.setBackgroundColor(getResources().getColor(
				android.R.color.background_light));
		
		if(savedInstanceState != null){
			if(mPosition == -1)
				mPosition = savedInstanceState.getInt(KEY_POSITION, -1);
			
			if(mAPKID == null)
				mAPKID = savedInstanceState.getString(InstalledExternalApplication.KEY_APK_ID, null);
		}
		
		if(mAPKID == null)
			Log.e(LOG_TAG, "onCreateView the APKID was not set and not in the bundle");
		
		// check the presence of the formID
		if(mFormID == -1){
			// the id is not set, it must be in the bundle
			mFormID = savedInstanceState.getInt(Form.KEY_FORM_ID, -1);
			if(mFormID == -1)
				// the id of the form was not in the bundle, this should never have happened
				Log.e(LOG_TAG, "onCreateView the formID was not set and not in the bundle");
		}
	
		ExternalApplication app;
		
		// get the corresponding installedApp
		if(mBelongsTo == WelcomeActivityPagerAdapter.TAB_HISTORY){
			app = HistoryExternalApplicationsManager.getInstance().getAppForId(mAPKID);
		}
		else{
		app = InstalledExternalApplicationsManager.getInstance().getAppForId(mAPKID);
		}
		
		if (app != null) {
			mForm = app.getSurvey().getForm(mFormID);
		}
	
	
		mRoot = (LinearLayout) inflater.inflate(
				R.layout.form, container, false);
		
		mScrollView = (ScrollView) mRoot.findViewById(R.id.scrollView1);
		
		// set focus to the dummy layout in order to prevent virtual keyboard from popping up
		View dummyLayout = mRoot.findViewById(R.id.dummy_layout_form);
		dummyLayout.requestFocus();
		
		addFormToLayout(mForm, mRoot);
		
		if(savedInstanceState != null){
			if(mIsFirst == null)
				mIsFirst = savedInstanceState.getBoolean(KEY_IS_FIRST);
			
			if(mIsLast == null)
				mIsLast = savedInstanceState.getBoolean(KEY_IS_LAST);
		}
		
		return (View) mRoot;
	}
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//============ HANDLING OF BUTTONS SHOWN IN FRAGMENT ========================
		LinearLayout formButtonContainer =  (LinearLayout) mLayoutInflater.inflate(R.layout.form_button_container, generateQuestionContainer((ViewGroup) mRoot.findViewById(R.id.ll_quest)));
		final ViewPager viewPager = (ViewPager) mRoot.getParent().getParent();
		Button buttonPrevious = (Button) formButtonContainer.findViewById(R.id.button_form_previous);
		Button buttonNext = (Button) formButtonContainer.findViewById(R.id.button_form_next);
		
		
		if(mIsFirst)
			buttonPrevious.setVisibility(View.GONE);
		else
			buttonPrevious.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int curPosition  = viewPager.getCurrentItem();
					viewPager.setCurrentItem(curPosition-1, true);
				}
			});
		
		if(mIsLast){
			buttonNext.setText(getString(R.string.q_send));
			buttonNext.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// User clicked SEND button
					
					// special care for text questions
		        	   Set<Question> questions = mQuestionEditTextMappings.keySet();
						for(Question question : questions){
							String finalAnswerOfUser = mQuestionEditTextMappings.get(question).getText().toString();
							question.setAnswer(finalAnswerOfUser);
						}
						
						// ===== MANDATORY QUESTION FILLED CHECK ====== //
						
						Survey theSurvey = InstalledExternalApplicationsManager.getInstance().getAppForId(mAPKID).getSurvey();
						List<Form> forms = theSurvey.getForms();
						Collections.sort(forms);
						// iterate over all forms and the over all questions and check if there is
						// a mandatory question that is not filled
						boolean mayBeSent = true; // set to true only if the survey may be sent
						for(Form form : forms){
							boolean formWithUnansweredQuestionFound = false;
							List<Question> questionsToCheck = form.getQuestions();
							Collections.sort(questionsToCheck);
							for(Question questionToCheck : questionsToCheck){
								if(questionToCheck.isMandatory()){
									// check if we have an answer
									if(questionToCheck.getAnswer().equals(Question.ANSWER_UNANSWERED)){
										// the question is unanswered although mandatory, take action
										mayBeSent = false;
										int formPosition = forms.indexOf(form);
										// go to the tab with containing the question
										Toaster.showToast(getActivity(), getString(R.string.notification_mandatory_question_unanswered));
										if(mPosition == formPosition){
											// the unanswered mandatory question is in this FormFragment
											// just scroll to the question (EditText representing the title of the question)
											scrollToQuestion(questionToCheck);
											formWithUnansweredQuestionFound = true;
											break;
										}
										else{
											// the question is not in this FormFragment
											// leave a message to this fragment and page to his position
											// that fragment should take care of scrolling
											Intent activityIntent = getActivity().getIntent();
											activityIntent.putExtra(KEY_POSITION_OF_FRAGMENT_WHICH_SHOULD_SCROLL, formPosition);
											activityIntent.putExtra(KEY_QUESTION_TO_SCROLL_TO, questionToCheck.getId());
											viewPager.setCurrentItem(formPosition, true);
											formWithUnansweredQuestionFound = true;
											break;
										}
									}
								}
							}
							if(formWithUnansweredQuestionFound)
								break;
							
						}
						// ===== END MANDATORY QUESTION CHECK  END ========= //
						
						if(mayBeSent){//  send to server only if all mandatory questions were filled
							
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							// Add the buttons
							builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   sendAnswersToServer();
							           }
							       });
							builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							               // User cancelled the dialog
							        	   dialog.dismiss();
							           }
							       });

							builder.setMessage(R.string.surveySendToServerMessage).setTitle(R.string.surveySendToServerTitle);
							
							// Create the AlertDialog
							AlertDialog dialog = builder.create();

							dialog.show();
						}
						}
				});
			if(mBelongsTo == WelcomeActivityPagerAdapter.TAB_HISTORY)
				buttonNext.setVisibility(View.GONE); // disable sending button if we are viewing the survey from history tab
			}
		else
			buttonNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int curPosition  = viewPager.getCurrentItem();
				viewPager.setCurrentItem(curPosition+1, true);
			}
		});
			
		//============ END HANDLING OF BUTTONS SHOWN IN FRAGMENT END ========================
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#setUserVisibleHint(boolean)
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			// oh I got selected, check if there is a message for me
			Intent activityIntent = getActivity().getIntent();
			int fragmentWhichShouldScroll = activityIntent.getIntExtra(KEY_POSITION_OF_FRAGMENT_WHICH_SHOULD_SCROLL, -1);
			if(fragmentWhichShouldScroll == mPosition){
				// it is me who should scroll, get that question id
				int questionId = activityIntent.getIntExtra(KEY_QUESTION_TO_SCROLL_TO, -1);
				// find the question instance
				for(Question aQuestion : mQuestionTitleMappings.keySet())
					if(aQuestion.getId() == questionId){
						// we have found the question, now scroll to it
						scrollToQuestion(aQuestion);
						break;
					}
				// remove the information from the intent, because consumed by this FormFragment
				activityIntent.removeExtra(KEY_POSITION_OF_FRAGMENT_WHICH_SHOULD_SCROLL);
				activityIntent.removeExtra(KEY_QUESTION_TO_SCROLL_TO);
				}
			}
	}

	/**
	 * Adds a form with all its questions to the ScrollView so that it is visible to the user.
	 * 
	 * @param form the form to be displayed
	 * @param ll the contained of the scrollview
	 */
	private void addFormToLayout(Form form, LinearLayout scrollViewContainer) {

		LinearLayout linearLayoutInsideAScrollView = (LinearLayout) scrollViewContainer.findViewById(R.id.ll_quest);
		
		List<Question> questions = form.getQuestions();
		Collections.sort(questions);

		// Check if there is at least a question to display
		if (questions.size() > 0) {
			
			for (int i = 0; i < questions.size(); i++) {
				Question question = questions.get(i);
				switch (questions.get(i).getType()) {
				case (Question.TYPE_SINGLE_CHOICE): {
					makeSingleChoice(questions.get(i), linearLayoutInsideAScrollView, i+1);
					break;
				}
				case (Question.TYPE_MULTIPLE_CHOICE): {
					makeMultipleChoice(questions.get(i), linearLayoutInsideAScrollView, i+1);
					break;
				}
				case (Question.TYPE_TEXT_QUESTION): {
					makeTextQuestion(questions.get(i), linearLayoutInsideAScrollView, i+1);
					break;
				}
				case (Question.TYPE_LIKERT_SCALE):{
					makeSingleChoice(question, linearLayoutInsideAScrollView, i+1);
					break;
				}
				case (Question.TYPE_YES_NO_QUESTION):{
					makeSingleChoice(question, linearLayoutInsideAScrollView, i+1);
					break;
				}
				default:
					break;
				}
			}
		} else {
			Log.w(LOG_TAG, "No Questions found in form["+mFormID+"]");
		}

	}

	/**
	 * Displays a single choice question to the user.
	 * @param question the question to be displayed
	 * @param linearLayoutInsideAScrollView the view to add the question to
	 * @param ordinal the ordinal number of the question i.e. 1, 2, 3, 4 or 5
	 */
	private void makeSingleChoice(final Question question, LinearLayout linearLayoutInsideAScrollView, int ordinal) {
		LinearLayout questionContainer = generateQuestionContainer(linearLayoutInsideAScrollView);
		String questionText = question.getTitle();
		List<PossibleAnswer> possibleAnswers = question.getPossibleAnswers();
		Collections.sort(possibleAnswers);

		TextView questionView = new TextView(getActivity());
		questionView.setText(ordinal + ". " + questionText);
		if(question.isMandatory())
			questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyleMandatory);
		else
			questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyle);
		questionContainer.addView(questionView);
		mQuestionTitleMappings.put(question, questionView);

		final RadioButton[] rb = new RadioButton[possibleAnswers.size()];
		RadioGroup rg = new RadioGroup(getActivity()); // create the RadioGroup
		rg.setOrientation(RadioGroup.VERTICAL);// or RadioGroup.VERTICAL
		String madeAnswer = question.getAnswer();
		int madeAnswerInt = -1;
		if(!madeAnswer.equals(Question.ANSWER_UNANSWERED))
			madeAnswerInt = Integer.parseInt(madeAnswer);
		
		for (int i = 0; i < rb.length; i++) {
			rb[i] = new RadioButton(getActivity());
			if(i%2==0)
				rb[i].setBackgroundColor(getActivity().getResources().getColor(R.color.light_gray));
			rg.addView(rb[i]); // the RadioButtons are added to the radioGroup
								// instead of the layout
			PossibleAnswer possibleAnswer = possibleAnswers.get(i);
			rb[i].setText(possibleAnswer.getTitle());
			final int possibleAnswerId = possibleAnswer.getId();
			if(madeAnswerInt == possibleAnswerId)
				rb[i].setChecked(true);
			rb[i].setTextAppearance(getActivity(), R.style.PossibleAnswerTextStyle);
			LayoutParams rowParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			rb[i].setLayoutParams(rowParam);
			
			// click handling
			rb[i].setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					question.setAnswer(String.valueOf(possibleAnswerId));
				}
			});
			if(mBelongsTo == WelcomeActivityPagerAdapter.TAB_HISTORY)
				rb[i].setEnabled(false);
			
			rb[i].setVisibility(View.VISIBLE);
		}
		
		rg.setVisibility(View.VISIBLE);
		if(mBelongsTo == WelcomeActivityPagerAdapter.TAB_HISTORY)
			rg.setEnabled(false);
		Log.i(LOG_TAG, "last rg = " + rg);
		questionContainer.addView(rg);
	}

	/**
	 * Displays a multiple choice question to the user.
	 * @param question the question to be displayed
	 * @param linearLayoutInsideAScrollView the view to add the question to
	 * @param ordinal the ordinal number of the question i.e. 1, 2, 3, 4 or 5
	 */
	private void makeMultipleChoice(final Question question, LinearLayout linearLayoutInsideAScrollView, int ordinal) {
		LinearLayout questionContainer = generateQuestionContainer(linearLayoutInsideAScrollView);
		String questionText = question.getTitle();
		List<PossibleAnswer> possibleAnswers = question.getPossibleAnswers();
		Collections.sort(possibleAnswers);

		TextView questionView = new TextView(getActivity());
		questionView.setText(ordinal + ". " + questionText);
		if(question.isMandatory())
			questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyleMandatory);
		else
			questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyle);
		questionContainer.addView(questionView);
		mQuestionTitleMappings.put(question, questionView);

		Log.i(LOG_TAG, "questionView = " + questionView.getText());
		
		final HashSet<String> madeAnswers = new HashSet<String>();
		madeAnswers.addAll(Arrays.asList(question.getAnswer().split(",")));
		madeAnswers.remove(""); // paranoia

		final CheckBox[] checkBoxs = new CheckBox[possibleAnswers.size()];
		for (int i = 0; i < checkBoxs.length; i++) {
			final PossibleAnswer possibleAnswer = possibleAnswers.get(i);
			final String possibleAnswerId = String.valueOf(possibleAnswer.getId());
			checkBoxs[i] = new CheckBox(getActivity());
			if(i%2 == 0)
				checkBoxs[i].setBackgroundColor(getActivity().getResources().getColor(R.color.light_gray));
			checkBoxs[i].setText(possibleAnswer.getTitle());
			checkBoxs[i].setTextAppearance(getActivity(), R.style.PossibleAnswerTextStyle);
			if(madeAnswers.contains(possibleAnswerId))
				checkBoxs[i].setChecked(true);
			
			// click handling
			checkBoxs[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked)
						madeAnswers.add(possibleAnswerId);
					else
						madeAnswers.remove(possibleAnswerId);
					String newAnswer = "";
					for(String madeAnswer1 : madeAnswers)
						newAnswer = newAnswer+","+madeAnswer1;
					if(!newAnswer.isEmpty())
						newAnswer=newAnswer.substring(1); // remove the leading ","
					question.setAnswer(newAnswer);
				}
			});
			
			checkBoxs[i].setVisibility(View.VISIBLE);
			if(mBelongsTo == WelcomeActivityPagerAdapter.TAB_HISTORY)
				checkBoxs[i].setEnabled(false);
			questionContainer.addView(checkBoxs[i]);
		}
	}

	/**
	 * Displays a text question to the user.
	 * @param question the question to be displayed
	 * @param linearLayoutInsideAScrollView the view to add the question to
	 * @param ordinal the ordinal number of the question i.e. 1, 2, 3, 4 or 5
	 */
	private void makeTextQuestion(final Question question, LinearLayout linearLayoutInsideAScrollView, int ordinal) {
		LinearLayout questionContainer = generateQuestionContainer(linearLayoutInsideAScrollView);
		TextView questionView = new TextView(getActivity());
		questionView.setText(ordinal + ". " + question.getTitle());
		if(question.isMandatory())
			questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyleMandatory);
		else
			questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyle);
		questionContainer.addView(questionView);
		mQuestionTitleMappings.put(question, questionView);

		final EditText editText = new EditText(getActivity());
		String madeAnswer = question.getAnswer();
		if(!madeAnswer.equals(Question.ANSWER_UNANSWERED))
			editText.setText(madeAnswer);
		
		if(mBelongsTo == WelcomeActivityPagerAdapter.TAB_HISTORY)
			editText.setEnabled(false);
		else{
			// remember the answer as soon as the edittext looses focus
			editText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						String newAnswer = editText.getText().toString();
						if(!newAnswer.equals(Question.ANSWER_UNANSWERED))
							question.setAnswer(newAnswer);
					}
				}
			});
		}
		
		editText.setVisibility(View.VISIBLE);
		if (question.getAnswer() != null) {
			editText.setText(question.getAnswer());
		}
		
		mQuestionEditTextMappings.put(question, editText);
		
		questionContainer.addView(editText);
	}
	
	/**
	 * This method creates a {@link LinearLayout} instance which will contain a question with possible
	 * answers.
	 * @param root the {@link ViewGroup} instance that will be the parent of the container
	 * @return a container for question and possible answers
	 */
	private LinearLayout generateQuestionContainer(ViewGroup root){
		LinearLayout container = (LinearLayout) mLayoutInflater.inflate(R.layout.question_container_layout, root);
		return container;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// save the formID for the future
		outState.putInt(Form.KEY_FORM_ID, mFormID);
		outState.putString(InstalledExternalApplication.KEY_APK_ID, mAPKID);
		outState.putBoolean(KEY_IS_FIRST, mIsFirst);
		outState.putBoolean(KEY_IS_LAST, mIsLast);
		outState.putInt(KEY_POSITION, mPosition);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Sets the ID of the {@link Form} instance which will be visualized by this {@link FormFragment}.
	 * 
	 * @param formID the id of the form that will be visualized.
	 */
	public void setFormID(int formID) {
		this.mFormID = formID;
	}

	/**
	 * Sets the ID of the {@link InstalledExternalApplication} whose {@link Survey} contains the {@link Form} which will
	 * be visualized by this {@link FormFragment}.
	 * 
	 * @param formID the id of the form that will be visualized.
	 */
	public void setAPKID(String mAPKID) {
		this.mAPKID = mAPKID;
	}

	/**
	 * Sets the flag for the first {@link FormFragment} in the list.
	 * @param isFirst the mIsFirst to set
	 */
	void setIsFirst(boolean isFirst) {
		this.mIsFirst = isFirst;
	}

	/**
	 * Sets the flag for the last {@link FormFragment} in the list.
	 * @param isLast the mIsLast to set
	 */
	void setIsLast(boolean isLast) {
		this.mIsLast = isLast;
	}
	
	
	//===========================================================================
		//================== SENDING ANSWERS TO SERVER ==============================
		//===========================================================================
		/**
		 * Sends this {@link Survey}'s answers to the server. If the sessionID
		 * is invalid it will try 2 more times, else it fails.
		 */
		public void sendAnswersToServer() {
			mNumberOfAttemptsToSendResultsToServer = 0;
			retrySendAnswersToServer();
		}
		
		/**
		 * Retries to send the answers.
		 */
		private void retrySendAnswersToServer(){
			mNumberOfAttemptsToSendResultsToServer++;		
			if (mNumberOfAttemptsToSendResultsToServer > 3){
				//
			} else {
				new RequestSendSurveyAnswers(new SendQuestionnaireAnswers(), MosesService.getInstance().getSessionID(), mAPKID).send();
				}
			}
		
		/**
		 * Implementation of {@link ReqTaskExecutor} to handle the return of the server
		 * for a SetQuestionnaireRequest
		 */
		private class SendQuestionnaireAnswers implements ReqTaskExecutor {
			@Override
			public void handleException(Exception e) {
				Log.d("SendQuestionnaireAnswers", "Failed because of an exception: " + e.getMessage());
			}

			@Override
			public void postExecution(String s) {
				try {
					JSONObject j = new JSONObject(s);
					Log.d(LOG_TAG, "postExecution return was: "+s);
					String Status = j.getString("STATUS");
					if (Status.equals("SUCCESS")){
						Log.d(LOG_TAG, "Successfully set the answers");
						Log.d(LOG_TAG, "IEA Manager = "+InstalledExternalApplicationsManager.getInstance()+" IEA = " + InstalledExternalApplicationsManager.getInstance().getAppForId(mAPKID).asOnelineString());
						InstalledExternalApplication apk = InstalledExternalApplicationsManager.getInstance().getAppForId(mAPKID);
						InstalledExternalApplicationsManager.getInstance().forgetExternalApplication(apk);
						HistoryExternalApplicationsManager.getInstance().addExternalApplication(new HistoryExternalApplication(apk, true, true)); // add the app to history
						Toaster.showToast(getActivity(), getString(R.string.notification_results_sent_to_server));
						// survey has been sent to server, set result to OK and finish the activity
						Activity activity = getActivity();
						activity.setResult(Activity.RESULT_OK);
						activity.finish();
					} else 
						if (Status.equals("INVALID_SESSION")){
						Log.d("SendQuestionnaireAnswers", "Failed to set the answers, because of invalid Session ID. Trying again");
						// Retries to send the answers
						MosesService.getInstance().login();
						retrySendAnswersToServer();
					}
						else
							Log.w(LOG_TAG, "postExecution() unknown error");
				} catch (JSONException e) {
					this.handleException(e);
				}
			}

			@Override
			public void updateExecution(BackgroundException c) {
				if (c.c == ConnectionParam.EXCEPTION) {
					handleException(c.e);
				}
			}
		}
		
		//===========================================================================
		//================== END SENDING RESULTS TO SERVER ==========================
		//===========================================================================

		/**
		 * Returns the position of this {@link FormFragment} instance in the view pager.
		 * @return the position of this fragment in the pager
		 */
		public int getPosition() {
			return mPosition;
		}


		/**
		 * Sets a variable in this {@link FormFragment} indicating its position
		 * in the view pager.
		 * @param position the position to set
		 */
		void setPosition(int position) {
			this.mPosition = position;
		}
		
		/**
		 * Scrolls the scroll view of this {@link FormFragment} instance to specified question
		 * so that it is visible to the user.
		 * @param questionToCheck question to scroll to
		 */
		private void scrollToQuestion(Question questionToCheck) {
			TextView textView = mQuestionTitleMappings.get(questionToCheck);
			// scroll to the top of the title
			mScrollView.smoothScrollTo(0, textView.getTop());
		}
		
		
	
	
}
