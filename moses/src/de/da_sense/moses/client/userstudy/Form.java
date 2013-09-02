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
package de.da_sense.moses.client.userstudy;

import java.util.List;

import android.os.Bundle;

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
	
	/**
	 * A constant to be used when passing an id of a {@link Form} through
	 * a communication channel. For example, through a {@link Bundle}.
	 */
	public static final String KEY_FORM_ID = "key_form_id";
	
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
