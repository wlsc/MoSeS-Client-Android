package de.da_sense.moses.client;

import java.io.IOException;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.Single_Questionnaire;
import de.da_sense.moses.client.service.helpers.Question;
import de.da_sense.moses.client.util.Log;

/**
 * questionnaires for a user study
 * 
 * @author Ibrahim Alyahya, Sandra Amend, Florian Schnell, Wladimir Schmidt
 * 
 */
public class QuestionnaireFragment extends Fragment {
	/**
	 * Defining a log tag to this class
	 */
	private static final String TAG = "QuestionnaireFragment";

	/** layout for the questionnaire */
	private LinearLayout ll;
	
	/**
	 * To send (and to save on local) user's answers of a questionnaire to
	 * server
	 */
	Button btnSend;

	/**
	 * The Single Questionnaire to display
	 */
	Single_Questionnaire usQuestionnaire;
	
	/**
	 * The APKID
	 */
	private String apkid = "";
	
	/**
	 * Index of the current Questionnaire
	 */
	private int currentIndex;
	
	/**
	 * Index of the last Questionnaire in the Multi_Questionnaire
	 */
	private int lastIndex;

	/**
	 * The "Next Questionnaire" Button
	 */
	private Button btnNext;

	/**
	 * The possible types of a question
	 */
	public static final int SINGLE_QUESTION = 1, MULTIPLE_QUESTION = 2,
			OPEN_QUESTION = 3;

	/**
	 * Remove the send button from the layout.
	 */
	private void removingButtonsFromThisLayout() {
		Log.d(TAG, "removingButtonsFormThisLayout: btnSend = " + btnSend);
		if (btnSend != null && btnSend.getVisibility() == View.VISIBLE)
			btnSend.setVisibility(View.INVISIBLE);
		Log.d(TAG, "removingButtonsFormThisLayout: btnNext = " + btnNext);
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
		final LinearLayout layout = (LinearLayout) ll
				.findViewById(R.id.ll_quest);
		LinearLayout bLayout = (LinearLayout) ll
				.findViewById(R.id.bottom_quest);
		if (currentIndex == lastIndex) {
		// Adding save button
		btnSend = new Button(getActivity().getApplicationContext());
		btnSend.setText("Send");
		btnSend.setTag("SendButton");
		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				usQuestionnaire.setAnswers(layout);
				Toast.makeText(getActivity().getApplicationContext(),
						"Your answers have been sent", Toast.LENGTH_LONG).show();
				InstalledExternalApplicationsManager.getInstance().getAppForId(apkid).getMultiQuestionnaire().sendAnswersToServer();
				// XXX Test ob es speichern kann
				try {
					InstalledExternalApplicationsManager.getInstance().saveToDisk(MosesService.getInstance());
				} catch (IOException e) {
					Log.d("Flo", "Konnte nicht speichern");
					e.printStackTrace();
				}
				// XXX Test ob es speichern kann
				v.setEnabled(false); // disable buttons
				}
			}
		);
		bLayout.addView(btnSend);
		} else {
		btnNext= new Button(getActivity().getApplicationContext());
		btnNext.setText("Next");
		btnNext.setTag("NextButton");
		btnNext.setEnabled(true);
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Sets he answers of the current Questionnaire in order
				// to swap to the next one				
				usQuestionnaire.setAnswers(layout);
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				QuestionnaireFragment nextFragment = new QuestionnaireFragment();
				nextFragment.setRetainInstance(true);
				Bundle args = getArguments();
				args.putInt("de.da_sense.moses.client.current", currentIndex + 1);
				nextFragment.setArguments(args);
				ft.replace(android.R.id.content, nextFragment);
				ft.commit();
			}
		});
		bLayout.addView(btnNext);
		}
	}

	/**
	 * Display a Questionnaire.
	 * 
	 * @param questionnaire
	 * @param ll
	 */
	private void displayQuestionnaire(Question[] questions, LinearLayout ll) {

		LinearLayout top_quest = (LinearLayout) ll.findViewById(R.id.ll_quest);
		Log.d(TAG, "top_quest = " + top_quest);

		// Check if there is at least a question to display
		if (questions.length > 0) {
			for (int i = 0; i < questions.length; i++) {
				switch (questions[i].getType()) {
				case (SINGLE_QUESTION): {
					makeSingleChoice(questions[i], top_quest, i);
					break;
				}
				case (MULTIPLE_QUESTION): {
					makeMultipleChoice(questions[i], top_quest, i);
					break;
				}
				case (OPEN_QUESTION): {
					makeOpenQuestion(questions[i], top_quest, i);
					break;
				}
				default:
					break;
				}
			}
		} else {
			Log.d(TAG, "No Questions found in questionnaire["+currentIndex+"]");
		}

	}

	/**
	 * To display a question with single choice type
	 * 
	 * @param question
	 *            the question to create
	 * @param ll
	 *            the layout to put this question on it
	 * @param qid
	 *            the question id of this question
	 */
	private void makeSingleChoice(Question question, LinearLayout ll, int qid) {
		String questionText = question.getQuestionText();
		String[] possibleAnswers = question.getPossibleAnswers();
		Log.d("QuestionnaireFragment", possibleAnswers[0] + possibleAnswers[1] + possibleAnswers[2]);

		TextView questionView = new TextView(getActivity());
		questionView.setText(questionText);

		ll.addView(questionView);

		final RadioButton[] rb = new RadioButton[possibleAnswers.length];
		RadioGroup rg = new RadioGroup(getActivity()); // create the
																// RadioGroup

		String rgTag = "Questionnaire" + qid;
		rg.setTag(rgTag);

		Log.i(TAG, "first rg = " + rg);
		rg.setOrientation(RadioGroup.VERTICAL);// or RadioGroup.VERTICAL
		for (int i = 0; i < rb.length; i++) {
			String tagString = rgTag + "Answer" + i;

			rb[i] = new RadioButton(getActivity());
			rg.addView(rb[i]); // the RadioButtons are added to the radioGroup
								// instead of the layout
			rb[i].setText(possibleAnswers[i]);
			rb[i].setTag(tagString);
			if (question.getAnswer() != null
					&& possibleAnswers[i].equals(question.getAnswer())) {
				Log.i(TAG, "setChecked for index = " + i);
				rb[i].setChecked(true);
			}
			rb[i].setVisibility(View.VISIBLE);
		}
		rg.setVisibility(View.VISIBLE);
		Log.i(TAG, "last rg = " + rg);
		ll.addView(rg);
	}

	/**
	 * To display a question with multiple choices type
	 * 
	 * @param question
	 *            the question to create
	 * @param ll
	 *            the layout to put this question on it
	 * @param qid
	 *            the question id of this question
	 */
	private void makeMultipleChoice(Question question, LinearLayout ll, int qid) {

		String questionText = question.getQuestionText();
		String[] possibleAnswers = question.getPossibleAnswers();
		String currentAnswers = question.getAnswer();

		TextView questionView = new TextView(getActivity());
		questionView.setText(questionText);
		ll.addView(questionView);

		Log.i(TAG, "questionView = " + questionView.getText());

		final CheckBox[] checkBoxs = new CheckBox[possibleAnswers.length];
		for (int i = 0; i < checkBoxs.length; i++) {
			String tagString = "Questionnaire" + qid + "Answer" + i;
			checkBoxs[i] = new CheckBox(getActivity());
			checkBoxs[i].setText(possibleAnswers[i]);
			checkBoxs[i].setVisibility(View.VISIBLE);
			checkBoxs[i].setTag(tagString);
			checkBoxs[i]
					.setChecked(currentAnswers.contains(possibleAnswers[i]));
			ll.addView(checkBoxs[i]);
		}
	}

	/**
	 * To display a question with open question type
	 * 
	 * @param question
	 *            the question text
	 * @param ll
	 *            the layout to put this question on it
	 * @param qid
	 *            the question id of this question
	 * @param answer
	 */
	private void makeOpenQuestion(Question question, LinearLayout ll, int qid) {
		TextView questionView = new TextView(getActivity());
		questionView.setText(question.getQuestionText());
		ll.addView(questionView);

		EditText editText = new EditText(getActivity());
		editText.setVisibility(View.VISIBLE);
		editText.setTag("Questionnaire" + qid);
		if (question.getAnswer() != null) {
			editText.setText(question.getAnswer());
		}
		ll.addView(editText);
	}

	/**
	 * Creates a Layout for a Single_Questionnaire
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		container.setBackgroundColor(getResources().getColor(
				android.R.color.background_light));

		apkid = "";
		if (savedInstanceState == null) {
			Log.d(TAG, "savedInstance == null");
			savedInstanceState = getArguments();
			Log.d(TAG, "NOW savedInstance = " + savedInstanceState);
		}

		// retrieve the arguments
		apkid = savedInstanceState
				.getString("de.da_sense.moses.client.apkid");
		savedInstanceState
				.getInt("de.da_sense.moses.client.belongsTo");
		Log.d(TAG, "\nretireved apkid = " + apkid);
		currentIndex = savedInstanceState.getInt("de.da_sense.moses.client.current");

		if (apkid != null) {
			Log.d(TAG, "savedInstanceState.apkid = " + apkid);
		} else {
			Log.d(TAG, "Error while retrieving APKID");
		}

		// get the corresponding installedApp
		InstalledExternalApplication app = InstalledExternalApplicationsManager
				.getInstance().getAppForId(apkid);
		
		if (app != null) {
			usQuestionnaire = app.getMultiQuestionnaire().getSingleQuestionnaire(currentIndex);
			lastIndex = app.getMultiQuestionnaire().getLastIndex();
		}
		Log.d(TAG, "usQuestionnaire = " + usQuestionnaire);
		if (usQuestionnaire == null) {
			Log.d(TAG, "Error while trying to get the "+currentIndex+"-th Single_Questionnaire");
		}

		ll = (LinearLayout) inflater.inflate(
				R.layout.questionnaire, container, false);
		// retrieve the chosen questionnaires for this US
		Question[] questions = usQuestionnaire.getQuestions();
		displayQuestionnaire(questions, ll);

		// TODO Disable Saving if it was already sent to the server
		if (!app.getMultiQuestionnaire().hasBeenSent()) {
			Log.i(TAG, "this questionnaire can be sent");
			addingButtonsToThisLayout(ll);
		} else {
			Log.i(TAG, "this questionnaire was sent");
			removingButtonsFromThisLayout();
		}
		return (View) ll;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		usQuestionnaire.setAnswers(ll);
		super.onSaveInstanceState(outState);
	}
	
}