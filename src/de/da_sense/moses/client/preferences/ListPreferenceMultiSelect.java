package de.da_sense.moses.client.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import de.da_sense.moses.client.R;

/**
 * This class provides support for selecting and filtering sensors on a device
 * @author Jaco Hofmann
 */
public class ListPreferenceMultiSelect extends ListPreference {
    /** separator between sensors defined */
	private static final String SEPARATOR = ",";
	/** to represent the selected sensors */
	private boolean[] mClickedDialogEntryIndices;
	/** to represent the sensors on screen */
	private int[] resourceIds = null;

	/**
	 * When an object created without any attribute
	 * @param context Context
	 * @see android.preference.ListPreference#ListPreference(Context)
	 */
	public ListPreferenceMultiSelect(Context context) {
        this(context, null);
    }
	
	/**
	 * When an object of ListPreferenceMultiSelect created with specific attributes
	 * @param context Context
	 * @param attrs AttributeSet
	 * @see android.preference.ListPreference#ListPreference(Context, AttributeSet)
	 */
	public ListPreferenceMultiSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Retrieve styled attribute information in this Context's theme
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceMultiSelect);
		// get a string array of sensors' images
		String[] imageNames = context.getResources().getStringArray(
				typedArray.getResourceId(typedArray.getIndexCount() - 1, -1));
		// resourceIds represents the sensors on screen
		resourceIds = new int[imageNames.length];
		for (int i = 0; i < imageNames.length; i++) {
			String imageName = imageNames[i].substring(imageNames[i].indexOf('/') + 1, imageNames[i].lastIndexOf('.'));
			resourceIds[i] = context.getResources().getIdentifier(imageName, null, context.getPackageName());
		}
		// Give back a previously retrieved StyledAttributes
		typedArray.recycle();
		// Initialize which sensors are selected
		mClickedDialogEntryIndices = new boolean[getEntries().length];
	}

	/*
     * @see android.preference.ListPreference#setEntries(CharSequence[])
     */
	@Override
	public void setEntries(CharSequence[] entries) {
		super.setEntries(entries);
		// Initialize which sensors are selected
		mClickedDialogEntryIndices = new boolean[entries.length];
	}

	/**
	 * to set the images of sensors
	 * @param context Context
	 * @param imageNames String[]
	 */
	public void setImages(Context context, String[] imageNames) {
		resourceIds = new int[imageNames.length];
		for (int i = 0; i < imageNames.length; i++) {
			String imageName = imageNames[i];
			resourceIds[i] = context.getResources().getIdentifier(imageName, null, context.getPackageName());
		}
	}

	/**
	 * Prepares the dialog builder to be shown when the preference is clicked.
	 * @see android.preference.ListPreference.onPrepareDialogBuilder(Builder)
	 */
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
	    // get the available sensors' names on this device
		CharSequence[] entries = getEntries();
		// get the available sensors' values on this device
		CharSequence[] entryValues = getEntryValues();
		if (entries == null || entryValues == null || entries.length != entryValues.length) {
			throw new IllegalStateException(
					"ListPreference requires an entries array and an entryValues array which are both the same length");
		}
		// This array represents the list of sensors as text
		CharSequence[] e = new CharSequence[getEntries().length + 1];
		e[0] = "All";
		for (int i = 0; i < getEntries().length; ++i) {
			e[i + 1] = getEntries()[i];
		}
		// restore the selected sensors
		restoreCheckedEntries();
		// Making an adapter for the list of sensors
		ListAdapter listAdapter = new ImageArrayAdapter(getContext(), R.layout.list_preference_multi_select, e,
				resourceIds, mClickedDialogEntryIndices);
		builder.setAdapter(listAdapter, this);
	}

	/**
	 * to parse a giving string of an array of sensors which should be written as [sensor1,sensor2,...] 
	 * @param val the sensors as CharSequence to parse
	 * @return a string array of sensors 
	 */
	public static String[] parseStoredValue(CharSequence val) {
		// determine if there is no sensor to parse
	    if (val == null || "[]".equals(val))
			return null;
		else
			return (((String) val).substring(1, val.length() - 1)).split(SEPARATOR);
	}

	/**
	 * to restore the selected sensors
	 */
	private void restoreCheckedEntries() {
	    // get the available sensors
		CharSequence[] entryValues = getEntryValues();
		// get the selected sensors
		String[] vals = parseStoredValue(getValue());
		// to save the changes on mClickedDialogEntryIndices
		if (vals != null) {
			for (int j = 0; j < vals.length; j++) {
				String val = vals[j].trim();
				for (int i = 0; i < entryValues.length; i++) {
					CharSequence entry = entryValues[i];
					if (entry.equals(val)) {
						mClickedDialogEntryIndices[i] = true;
						break;
					}
				}
			}
		}
	}

	/**
     * check if there any thing changed after closing the dialog
     * @param  positiveResult whether user clicked OK or not
     * @see android.preference.ListPreference.onDialogClosed(boolean)
     */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
	    // get entry values
		CharSequence[] entryValues = getEntryValues();
		// if the user clicked ok
		if (positiveResult && entryValues != null) {
		    // read the selected sensors
			StringBuffer value = new StringBuffer();
			value.append('[');
			for (int i = 0; i < entryValues.length; i++) {
				if (mClickedDialogEntryIndices[i]) {
					value.append(entryValues[i]).append(SEPARATOR);
				}
			}
			if (value.length() > 1)
				value.deleteCharAt(value.length() - 1);
			value.append(']');
			// if the user changed the pref
			if (callChangeListener(value)) {
				String val = value.toString();
				setValue(val);
			}
		}
	}
}
