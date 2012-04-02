package moses.client.abstraction.apks;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

/**
 * Reference to an application on the server, referenced by it's MoSeS id
 * 
 * @author Simon L
 * 
 */
public class ExternalApplication {
	private static final String TAG_NEWESTVERSION = "[newestversion]";

	private static final String TAG_DESCRIPTION = "[description]";

	private static final String TAG_NAME = "[name]";
	
	private static final String TAG_SENSORS = "[sensors]";

	private static final String SEPARATOR = "#EA#";
	
	private String ID;
	
	// lazy loading variables for non-defining attributes
	private String name;
	private String description;
	private String newestVersion="0";
	private List<Integer> sensors = null;

	public String getID() {
		return ID;
	}

	/**
	 * Creates a reference to an external application, specifying its ID
	 * 
	 * @param ID the id in the MoSeS database
	 */
	public ExternalApplication(String ID) {
		this.ID = ID;
	}

	/**
	 * sets the name of the application
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * sets the description of the application
	 * 
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the name of this external application. If the name was not set,
	 * or retrieved yet, this will be done now. In this case, this method should
	 * not be called from the UI thread (method blocks).
	 * 
	 * @return
	 */
	public String getName() {
		if (name != null) {
			return name;
		} else {
			return getGenericName();
		}
	}

	/**
	 * retrieves the name of this application from the server in case it was not
	 * set yet.
	 * 
	 * @return the name of the application
	 */
	private String getGenericName() {
		// TODO: implement, placeholder
		return "loading Name...  (ID: " + getID() + ")";
	}

	/**
	 * Returns the description of this external application. If the description
	 * was not set, or retrieved yet, this will be done now. In this case, this
	 * method should not be called from the UI thread (method blocks).
	 * 
	 * @return
	 */
	public String getDescription() {
		if (description != null) {
			return description;
		} else {
			return retrieveDescription();
		}
	}

	/**
	 * retrieves the description of this application from the server in case it
	 * was not set yet.
	 * 
	 * @return the description of the application
	 */
	private String retrieveDescription() {
		return "loading Description...";
	}

	public String getNewestVersion() {
		return newestVersion;
	}

	public List<Integer> getSensors() {
		return sensors;
	}

	public void setSensors(Collection<Integer> sensors) {
		if(sensors == null) {
			this.sensors = null;
		} else {
			this.sensors = new LinkedList(sensors);
		}
	}
	
	public boolean isSensorsSet() {
		return sensors != null;
	}

	public void setNewestVersion(String newestVersion) {
		this.newestVersion = newestVersion;
	}
	
	public boolean isNewestVersionSet() {
		return newestVersion != null;
	}

	/**
	 * @return whether the description of the application was already
	 *         retrieved/set
	 */
	public boolean isDescriptionSet() {
		return description != null;
	}

	/**
	 * @return whether the name of the application was already retrieved/set
	 */
	public boolean isNameSet() {
		return name != null;
	}
	
	/**
	 * writes this object into an one-line string
	 * 
	 * @return the encoded object
	 */
	public String asOnelineString() { //ID-{name}-{description}
		String result = this.ID;
		if(isNameSet()) {
			result += SEPARATOR + TAG_NAME + getName();
		}
		if(isDescriptionSet()) {
			result += SEPARATOR + TAG_DESCRIPTION + getDescription();
		}
		if(isNewestVersionSet()) {
			result += SEPARATOR + TAG_NEWESTVERSION + getNewestVersion().toString();
		}
		if(isSensorsSet()) {
			result += SEPARATOR + TAG_SENSORS + new JSONArray(getSensors()).toString();
		}
		return result;
	}
	
	@Override
	public String toString() {
		return asOnelineString();
	}
	
	/**
	 * creates an external application from a string (@see {@link #asOnelineString()})
	 * 
	 * @param s the string-exncoded external application
	 * @return the decoded external application
	 */
	public static ExternalApplication fromOnelineString(String s) {
		String[] split = s.split(Pattern.quote(SEPARATOR));
		String ID = null;
		String name = null;
		String description = null;
		String newestVersion = null;
		List<Integer> sensors = null;
		for(int i=0; i<split.length; i++) {
			if(i==0) {
				ID = split[i];
			} else {
				if(split[i].startsWith(TAG_DESCRIPTION)) {
					description = split[i].substring(TAG_DESCRIPTION.length());
				}
				if(split[i].startsWith(TAG_NAME)) {
					name = split[i].substring(TAG_NAME.length());
				}
				if(split[i].startsWith(TAG_NEWESTVERSION)) {
					newestVersion = split[i].substring(TAG_NEWESTVERSION.length());
				}
				if(split[i].startsWith(TAG_SENSORS)) {
					sensors = new LinkedList<Integer>();
					JSONArray jsonarray = null;
					try {
						jsonarray = new JSONArray(split[i].substring(TAG_SENSORS.length()));
						for(int j=0; j<jsonarray.length(); j++) sensors.add(jsonarray.getInt(j));
					} catch (JSONException e) {
						Log.e("MoSeS.APK", "error parsing external application from settings file", e);
					}
				}
			}
		}
		
		ExternalApplication externalApplication = new ExternalApplication(ID);
		externalApplication.setName(name);
		externalApplication.setDescription(description);
		externalApplication.setNewestVersion(newestVersion);
		externalApplication.setSensors(sensors);
		return externalApplication;
	}

	/**
	 * @return true if all data that could be retrieved for this object (name, description, sensors, ...) is held in this object
	 */
	public boolean isDataComplete() {
		return isDescriptionSet() && isNameSet() && isSensorsSet() && isNewestVersionSet();
	}
	
}
