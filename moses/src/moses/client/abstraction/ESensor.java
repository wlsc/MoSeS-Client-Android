/**
 * 
 */
package moses.client.abstraction;

/**
 * Enumeration of all Sensors, their ordinals and String-representations
 * @author Zijad
 *
 */
public enum ESensor {
	
	/*
	 * Just a dummy value, because on android, sensor ordinals start with 1 
	 */
	UNKNOWN {
		public String toString(){
			return "Unknown sensor";
		}
	},
	
	ACCELEROMETER {
		public String toString(){
			return "Accelerometer";
		}
	},
	
	MAGNETIC_FIELD_SENSOR{
		public String toString(){
			return "Magnetic field sensor";
		}
	},
	
	ORIENTATION_SENSOR{
		public String toString(){
			return "Orientation sensor";
		}
	},
	
	GYROCSCOPE {
		public String toString(){
			return "Gyroscope";
		}
	},
	
	LIGHT_SENSOR{
		public String toString(){
			return "Light sensor";
		}
	},
	
	PREASSURE_SENSOR{
		public String toString(){
			return "Preassure sensor";
		}
	},
	
	TEMPERATURE_SENSOR{
		public String toString(){
			return "Temperature sensor";
		}
	},
	
	PROXIMITY_SENSOR{
		public String toString(){
			return "Proximity sensor";
		}
	},
	
	GRAVITY_SENSOR{
		public String toString(){
			return "Gravity sensor";
		}
	},
	
	LINEAR_ACCELERATION_SENSOR{
		public String toString(){
			return "Linear acceleration sensor";
		}
	},
	
	ROTATION_SENSOR{
		public String toString(){
			return "Rotation sensor";
		}
	},
	
	HUMIDITY_SENSOR{
		public String toString(){
			return "Humidity sensor";
		}
	},
	
	AMBIENT_TEMPERATURE_SENSOR{
		public String toString(){
			return "Ambient temperature sensor";
		}
	};
	
	
}
