/**
 * 
 */
package de.da_sense.moses.client.abstraction;

import de.da_sense.moses.client.R;

/**
 * Enumeration of all Sensors, their ordinals and String-representations
 * 
 * @author Zijad
 * 
 */
public enum ESensor {

	/*
	 * Just a dummy value, because on android, sensor ordinals start with 1
	 */
	UNKNOWN("Unknown sensor", "no", 0), ACCELEROMETER("Accelerometer", "@drawable/accelerometer",
			R.drawable.accelerometer), MAGNETIC_FIELD_SENSOR("Magnetic field sensor", "@drawable/magnetic",
			R.drawable.magnetic), ORIENTATION_SENSOR("Orientation sensor", "@drawable/orientation",
			R.drawable.orientation), GYROCSCOPE("Gyroscope", "@drawable/gyroscope", R.drawable.gyroscope), LIGHT_SENSOR(
			"Light sensor", "@drawable/light", R.drawable.light), PRESSURE_SENSOR("Pressure sensor",
			"@drawable/pressure", R.drawable.pressure), TEMPERATURE_SENSOR("Temperature sensor",
			"@drawable/temperature", R.drawable.temperature), PROXIMITY_SENSOR("Proximity sensor",
			"@drawable/proximity", R.drawable.proximity), GRAVITY_SENSOR("Gravity sensor", "@drawable/gravity",
			R.drawable.gravity), LINEAR_ACCELERATION_SENSOR("Linear acceleration sensor", "@drawable/linear",
			R.drawable.linear), ROTATION_SENSOR("Rotation sensor", "@drawable/rotation", R.drawable.rotation), HUMIDITY_SENSOR(
			"Humidity sensor", "@drawable/humidity", R.drawable.humidity), AMBIENT_TEMPERATURE_SENSOR(
			"Ambient temperature sensor", "@drawable/ambient", R.drawable.ambient);

	private final String name;
	private final String image;
	private final int imageID;

	ESensor(String name, String image, int imageID) {
		this.name = name;
		this.image = image;
		this.imageID = imageID;
	}

	@Override
	public String toString() {
		return name;
	}

	public String image() {
		return image;
	}

	public int imageID() {
		return imageID;
	}

}
