package de.da_sense.moses.client.service.helpers;

import de.da_sense.moses.client.util.Log;

/**
 * Class for a single question.
 */
public class Question {
	
	/**
	 * The question type of this question 1 = Single Choice 2 = Multiple Choices
	 * 3 = Open Question
	 */
	private int type;
	
	/**
	 * The Separator used to separate Questions inside a Questionnaire-String
	 */
	private static final String QUESTIONNAIRE_SEPARATOR = "#SG#";
	
	/**
	 * The Separator used to separate parts inside a Question-String
	 */
	private static final String QUESTION_SEPARATOR = "#sep#";	
	
	/**
	 * The Separator used to separate different answers inside the answer-String
	 */
	private static final String ANSWER_SEPARATOR = "#SA#";
	
	/**
	 * The TAG
	 */
	private static final String TAG = "Question";
	
	/**
	 * The question text of this question
	 */
	private String questionText;
	
	/**
	 * The possible answers for this question.
	 */
	private String possibleAnswers;
	
	/**
	 * The user's answer of this question
	 */
	private String answer;
	
	/**
	 * The question's id.
	 */
	private int qid;

	/**
	 * Constructor for a question.
	 * 
	 * @param question
	 *            A string consisting of type, qid, questionText, answerPossibilities
	 *            and answer all separated by QUESTION_SEPARATOR
	 */
	public Question(String question) {
		Log.d(TAG, "Input was " + question);
		String[] splitted = question.split(QUESTION_SEPARATOR);
		Log.d(TAG, "Has " + splitted.length + " parts separated by "+ QUESTION_SEPARATOR);
		this.type = Integer.parseInt(splitted[0]);
		this.qid = Integer.parseInt(splitted[1]);
		this.questionText = splitted[2];
		this.possibleAnswers = (type != 3) ? splitted[3] : "";
		this.answer = (splitted.length == 5) ? splitted[4] : "";
	}

	/**
	 * Returns a string consisting of type, questionText, possibleAnswer, 
	 * answer all separated with QUESTION_SEPARATOR
	 * 
	 * @return String
	 */
	public String toString() {
		return String.valueOf(type).concat(QUESTION_SEPARATOR).concat(String.valueOf(qid)).concat(QUESTION_SEPARATOR).concat(questionText)
				.concat(QUESTION_SEPARATOR).concat(possibleAnswers).concat(QUESTION_SEPARATOR)
				.concat(answer);
	}

	/**
	 * @return the question text
	 */
	public String getQuestionText() {
		return questionText;
	}

	/**
	 * Returns the answer
	 * 
	 * @return answer
	 */
	public String getAnswer() {
		return answer;
	}

	/**
	 * Sets the answer
	 * 
	 * @param answer
	 */
	public void setAnswer(String answer) {
		this.answer = answer;
	}

	/**
	 * Returns the type of the Question
	 * 
	 * @return type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the possible Answers of the Question
	 * 
	 * @return possibleAnswers
	 */
	public String[] getPossibleAnswers() {
		Log.d("Question", possibleAnswers);
		return possibleAnswers.split(ANSWER_SEPARATOR);
	}

	public Integer getQID() {
		return qid;
	}
}
