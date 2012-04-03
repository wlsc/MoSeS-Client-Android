package de.da_sense.moses.client.preferences;

import de.da_sense.moses.client.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.ListAdapter;

/**
 * A {@link Preference} that displays a list of entries as a dialog and allows
 * multiple selections
 * <p>
 * This preference will store a string into the SharedPreferences. This string
 * will be the values selected from the {@link #setEntryValues(CharSequence[])}
 * array.
 * </p>
 */
public class ListPreferenceMultiSelect extends ListPreference {
	private static final String SEPARATOR = ",";

	private boolean[] mClickedDialogEntryIndices;

	private int[] resourceIds = null;

	public ListPreferenceMultiSelect(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceMultiSelect);

		String[] imageNames = context.getResources().getStringArray(
				typedArray.getResourceId(typedArray.getIndexCount() - 1, -1));

		resourceIds = new int[imageNames.length];

		for (int i = 0; i < imageNames.length; i++) {
			String imageName = imageNames[i].substring(imageNames[i].indexOf('/') + 1, imageNames[i].lastIndexOf('.'));

			resourceIds[i] = context.getResources().getIdentifier(imageName, null, context.getPackageName());
		}

		typedArray.recycle();

		mClickedDialogEntryIndices = new boolean[getEntries().length];
	}

	@Override
	public void setEntries(CharSequence[] entries) {
		super.setEntries(entries);
		mClickedDialogEntryIndices = new boolean[entries.length];
	}

	public void setImages(Context context, String[] imageNames) {
		resourceIds = new int[imageNames.length];

		for (int i = 0; i < imageNames.length; i++) {
			String imageName = imageNames[i];
			resourceIds[i] = context.getResources().getIdentifier(imageName, null, context.getPackageName());
		}
	}

	public ListPreferenceMultiSelect(Context context) {
		this(context, null);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {

		CharSequence[] entries = getEntries();
		CharSequence[] entryValues = getEntryValues();

		if (entries == null || entryValues == null || entries.length != entryValues.length) {
			throw new IllegalStateException(
					"ListPreference requires an entries array and an entryValues array which are both the same length");
		}
		CharSequence[] e = new CharSequence[getEntries().length + 1];
		e[0] = "All";
		for (int i = 0; i < getEntries().length; ++i) {
			e[i + 1] = getEntries()[i];
		}
		restoreCheckedEntries();
		ListAdapter listAdapter = new ImageArrayAdapter(getContext(), R.layout.list_preference_multi_select, e,
				resourceIds, mClickedDialogEntryIndices);
		builder.setAdapter(listAdapter, this);
	}

	public static String[] parseStoredValue(CharSequence val) {
		if (val == null || "[]".equals(val))
			return null;
		else
			return (((String) val).substring(1, val.length() - 1)).split(SEPARATOR);
	}

	private void restoreCheckedEntries() {
		CharSequence[] entryValues = getEntryValues();

		String[] vals = parseStoredValue(getValue());
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

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// super.onDialogClosed(positiveResult);

		CharSequence[] entryValues = getEntryValues();
		if (positiveResult && entryValues != null) {
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

			if (callChangeListener(value)) {
				String val = value.toString();
				setValue(val);
			}
		}
	}
}
