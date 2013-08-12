package de.da_sense.moses.client.userstudy;

/**
 * This class represents one or more possible answers of a {@link Question}.
 * @author Zijad Maksuti
 *
 */
public class PossibleAnswer extends HasID implements IHasTitle{
	
	private String mTitle;

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
	
	
	
}
