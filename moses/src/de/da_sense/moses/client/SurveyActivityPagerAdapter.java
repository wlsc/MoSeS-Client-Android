package de.da_sense.moses.client;

import java.util.Collections;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.userstudy.Form;
import de.da_sense.moses.client.userstudy.Survey;

/**
 * 
 * The {@link android.support.v4.view.PagerAdapter} that will provide {@link FormFragment}s representing
 * each {@link Form}.
 * 
 * @author Zijad Maksuti
 *
 */
class SurveyActivityPagerAdapter extends FragmentStatePagerAdapter{
	
	/**
	 * The ID of the {@link InstalledExternalApplication} instance containing the {@link Survey} with {@link Form}s
	 * which need to be visualized by the {@link FormFragment} instances returned by this {@link SurveyActivityPagerAdapter}.
	 */
	private String mAPKID;
	
	/**
	 * The list of {@link Form} instances of the survey.<p>
	 * <b>Note that this list is sorted,
	 * not like the one by calling {@link Survey#getForms()} although their elements are equivalent.</b>
	 */
	private List<Form> mForms;
	
	/**
	 * Constructs a new {@link SurveyActivityPagerAdapter} based on the consumed arguments.
	 * @param fm an instance of {@link FragmentManager}
	 * @param apk an {@link InstalledExternalApplication} containing the {@link Form} instances for which this adapter instantiates
	 * {@link FormFragment}s.
	 */
	public SurveyActivityPagerAdapter(FragmentManager fm, InstalledExternalApplication apk) {
		super(fm);
		this.mAPKID = apk.getID();
		this.mForms = apk.getSurvey().getForms();
		Collections.sort(mForms); // sort the forms
    }

    @Override
    public Fragment getItem(int i) {
        FormFragment formFragment = new FormFragment();
        formFragment.setFormID(mForms.get(i).getId());
        formFragment.setAPKID(mAPKID);
        
        if(i==0)
        	formFragment.setIsFirst(true);
        else
        	formFragment.setIsFirst(false);
        
        if(i==getCount()-1)
        	formFragment.setIsLast(true);
        else
        	formFragment.setIsLast(false);
        
        return formFragment;
    }

    @Override
    public int getCount() {
        return mForms.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mForms.get(position).getTitle();
    }
	
}
