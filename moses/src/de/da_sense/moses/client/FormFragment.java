package de.da_sense.moses.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
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
 * @author Ibrahim Alyahya, Sandra Amend, Florian Schnell, Wladimir Schmidt
 * @author Zijad Maksuti
 */
public class FormFragment extends Fragment {
	/**
	 * Defining a log tag to this class
	 */
	private static final String LOG_TAG = FormFragment.class.getName();
	
	/**
	 * To send (and to save on local) user's answers of a questionnaire to
	 * server
	 */
	private Button btnSend;

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

	/**
	 * The "Next Questionnaire" Button
	 */
	private Button btnNext;
	
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

	/**
	 * Creates a Layout for a Single_Questionnaire
	 * 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mLayoutInflater = inflater;
	
		container.setBackgroundColor(getResources().getColor(
				android.R.color.background_light));
	
		if(mAPKID == null)
			if(savedInstanceState != null)
				mAPKID = savedInstanceState.getString(InstalledExternalApplication.KEY_APK_ID, null);
		
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
	
		// get the corresponding installedApp
		InstalledExternalApplication app = InstalledExternalApplicationsManager
				.getInstance().getAppForId(mAPKID);
		
		if (app != null) {
			mForm = app.getSurvey().getForm(mFormID);
		}
	
	
		mRoot = (LinearLayout) inflater.inflate(
				R.layout.form, container, false);
		
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
		
		
		
	
		// TODO Disable Saving if it was already sent to the server
		if (!app.getSurvey().hasBeenSent()) {
			Log.i(LOG_TAG, "this questionnaire can be sent");
			addingButtonsToThisLayout(mRoot);
		} else {
			Log.i(LOG_TAG, "this questionnaire was sent");
			removingButtonsFromThisLayout();
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
					Toaster.showToast(getActivity(), "implement me");
				}
			});
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



	/**
	 * Remove the send button from the layout.
	 */
	private void removingButtonsFromThisLayout() {
		Log.d(LOG_TAG, "removingButtonsFormThisLayout: btnSend = " + btnSend);
		if (btnSend != null && btnSend.getVisibility() == View.VISIBLE)
			btnSend.setVisibility(View.INVISIBLE);
		Log.d(LOG_TAG, "removingButtonsFormThisLayout: btnNext = " + btnNext);
		if (btnNext != null && btnNext.getVisibility() == View.VISIBLE)
			btnNext.setVisibility(View.INVISIBLE);
	}

	/**
	 * Adds Buttons to this Layout. If the current questionnaire's index
	 * is not the last index it adds a Next Button, else an Send Button 
	 * @param layout
	 *            the Linear layout to be added on
	 */
	private void addingButtonsToThisLayout(LinearLayout ll) {
//		LinearLayout bLayout = (LinearLayout) ll.findViewById(R.id.bottom_quest);
//		XXX ZM CLEAN
//		if (currentIndex == lastIndex) {
//		// Adding save button
//		btnSend = new Button(getActivity().getApplicationContext());
//		btnSend.setText(getString(R.string.q_send));
//		btnSend.setTag("SendButton");
//		btnSend.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				usQuestionnaire.setAnswers(layout);
//				Toast.makeText(getActivity().getApplicationContext(), getString(R.string.q_answersSent), Toast.LENGTH_LONG).show();
////				InstalledExternalApplicationsManager.getInstance().getAppForId(apkid).getSurvey().sendAnswersToServer();
//				// XXX Test ob es speichern kann
//				try {
//					InstalledExternalApplicationsManager.getInstance().saveToDisk(MosesService.getInstance());
//				} catch (IOException e) {
//					Log.d("Flo", "Konnte nicht speichern");
//					e.printStackTrace();
//				}
//				// XXX Test ob es speichern kann
//				v.setEnabled(false); // disable buttons
//				}
//			}
//		);
//		bLayout.addView(btnSend);
//		} else {
//		btnNext= new Button(getActivity().getApplicationContext());
//		btnNext.setText(getString(R.string.q_next));
//		btnNext.setTag("NextButton");
//		btnNext.setEnabled(true);
//		btnNext.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// Sets he answers of the current Questionnaire in order
//				// to swap to the next one				
////				usQuestionnaire.setAnswers(layout);
//				FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
//				QuestionnaireFragment nextFragment = new QuestionnaireFragment();
//				nextFragment.setRetainInstance(true);
//				Bundle args = getArguments();
//				nextFragment.setArguments(args);
//				ft.replace(android.R.id.content, nextFragment);
//				ft.commit();
//			}
//		});
//		bLayout.addView(btnNext);
//		}
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
		questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyle);
		questionContainer.addView(questionView);

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
			
			rb[i].setVisibility(View.VISIBLE);
		}
		
		rg.setVisibility(View.VISIBLE);
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
		questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyle);
		questionContainer.addView(questionView);

		Log.i(LOG_TAG, "questionView = " + questionView.getText());
		
		final HashSet<String> madeAnswers = new HashSet<String>();
		madeAnswers.addAll(Arrays.asList(question.getAnswer().split(",")));

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
					newAnswer.replaceFirst(",", "");
					question.setAnswer(newAnswer);
				}
			});
			
			checkBoxs[i].setVisibility(View.VISIBLE);
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
		questionView.setTextAppearance(getActivity(), R.style.QuestionTextStyle);
		questionContainer.addView(questionView);

		final EditText editText = new EditText(getActivity());
		String madeAnswer = question.getAnswer();
		if(!madeAnswer.equals(Question.ANSWER_UNANSWERED))
			editText.setText(madeAnswer);
		
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
		
		editText.setVisibility(View.VISIBLE);
		if (question.getAnswer() != null) {
			editText.setText(question.getAnswer());
		}
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
	
}