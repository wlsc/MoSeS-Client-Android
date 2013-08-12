package de.da_sense.moses.client.userstudy;

import java.util.List;

/**
 * This class represents a form which contains one or more {@link Question} instances. A form is
 * contained in a {@link Survey}.
 * 
 * @author Zijad Maksuti
 *
 */
public class Form extends HasID implements IHasTitle {
	
	private String mTitle;
	
	private List<Question> mQuestions;
	
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
	 * Returns all {@link Question} instances attached to this form.
	 * @return all questions attached to this form if any
	 */
	public List<Question> getQuestions() {
		return mQuestions;
	}

	/**
	 * Sets a list of {@link Question} instances to this form.
	 * @param mQuestions the questions to set
	 */
	public void setQuestions(List<Question> questions) {
		this.mQuestions = questions;
	}

}
