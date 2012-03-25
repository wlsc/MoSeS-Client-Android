/**
 * 
 */
package moses.client.abstraction;

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
	UNKNOWN("Unknown sensor","no"),
	ACCELEROMETER("Accelerometer","@drawable/accelerometer"),
	MAGNETIC_FIELD_SENSOR("Magnetic field sensor","@drawable/magnetic"),
	ORIENTATION_SENSOR("Orientation sensor","@drawable/orientation"),
	GYROCSCOPE("Gyroscope","@drawable/gyroscope"),
	LIGHT_SENSOR("Light sensor","@drawable/light"),
	PRESSURE_SENSOR("Pressure sensor","@drawable/pressure"),
	TEMPERATURE_SENSOR("Temperature sensor","@drawable/temperature"),
	PROXIMITY_SENSOR("Proximity sensor","@drawable/proximity"),
	GRAVITY_SENSOR("Gravity sensor","@drawable/gravity"),
	LINEAR_ACCELERATION_SENSOR("Linear acceleration sensor","@drawable/linear"),
	ROTATION_SENSOR("Rotation sensor","@drawable/rotation"),
	HUMIDITY_SENSOR("Humidity sensor","@drawable/humidity"),
	AMBIENT_TEMPERATURE_SENSOR("Ambient temperature sensor","@drawable/ambient");

	private final String name;
	private final String image;

	ESensor(String name, String image) {
		this.name = name;
		this.image = image;
	}

	public String toString() {
		return name;
	}

	public String image() {
		return image;
	}

}
