package de.da_sense.moses.client.userstudy;

import java.util.List;


/**
 * This class represents a single Question. A question is contained in an
 * instance of of a {@link Form}. Depending of its type, a question may contains a {@link List} of {@link PossibleAnswer}
 * instances. The question types that contain possible answers are
 * {@link Question#TYPE_MULTIPLE_CHOICE} and {@link Question#TYPE_SINGLE_CHOICE}.
 */
public class Question extends HasID implements IHasTitle{
	
	//=================QUESTION TYPES===========================
	public static final int TYPE_YES_NO_QUESTION = 1;
	public static final int TYPE_TEXT_QUESTION = 2;
	public static final int TYPE_LIKERT_SCALE = 3;
	public static final int TYPE_MULTIPLE_CHOICE = 4;
	public static final int TYPE_SINGLE_CHOICE = 5;
	//=============END QUESTION TYPES END=======================
	
	private int mType;
	
	private String mTitle;
	
	/**
	 * Sets the type of this question.
	 * @param mType the type to set
	 */
	public void setType(int type) {
		this.mType = type;
	}

	private List<PossibleAnswer> mPossibleAnswers;
	
	/**
	 * Returns the type of the Question.
	 * 
	 * @return type
	 */
	public int getType() {
		return mType;
	}

	@Override
	public String setTitle(String title) {
		String oldTitle = mTitle;
		mTitle = title;
		return oldTitle;
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Returns all {@link PossibleAnswer} instances attached to this question.
	 * @return all possible answers of this question, if the question does not
	 * have any possible question, the method returns null
	 */
	public List<PossibleAnswer> getPossibleAnswers() {
		return mPossibleAnswers;
	}

	/**
	 * Sets the list of {@link PossibleAnswer} instances to this question.
	 * @param possibleAnswers the answers to set
	 */
	public void setPossibleAnswers(List<PossibleAnswer> possibleAnswers) {
		this.mPossibleAnswers = possibleAnswers;
	}

	/**
	 * Returns the answer the user gave to this question if any.
	 * @return {@link String} containing the answer to this question.
	 */
	public String getAnswer() {
		// TODO IMPLEMENT ME
		return null;
	}
}
