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
/**
 * 
 */
package de.da_sense.moses.client.abstraction;

import de.da_sense.moses.client.R;
import de.da_sense.moses.client.service.MosesService;

/**
 * Enumeration of all Sensors, their ordinal and String-representations
 * 
 * @author Zijad, Wladimir Schmidt
 * 
 * @deprecated this enum is marked for deletion
 * 
 */
public enum SensorsEnum {
	
	UNKNOWN(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_unknown), "no", 0), 
	ACCELEROMETER(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_accelerometer), "@drawable/accelerometer",R.drawable.accelerometer), 
	MAGNETIC_FIELD_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_magnetic_field), "@drawable/magnetic",R.drawable.magnetic), 
	ORIENTATION_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_orientation), "@drawable/orientation",R.drawable.orientation), 
	GYROCSCOPE(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_gyroscope), "@drawable/gyroscope", R.drawable.gyroscope), 
	LIGHT_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_light), "@drawable/light", R.drawable.light), 
	PRESSURE_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_pressure),"@drawable/pressure", R.drawable.pressure), 
	TEMPERATURE_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_temperature),"@drawable/temperature", R.drawable.temperature), 
	PROXIMITY_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_proximity),"@drawable/proximity", R.drawable.proximity), 
	GRAVITY_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_gravity), "@drawable/gravity",R.drawable.gravity), 
	LINEAR_ACCELERATION_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_linear_acceleration), "@drawable/linear",R.drawable.linear), 
	ROTATION_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_rotation), "@drawable/rotation", R.drawable.rotation), 
	HUMIDITY_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_humidity), "@drawable/humidity", R.drawable.humidity), 
	AMBIENT_TEMPERATURE_SENSOR(MosesService.getInstance().getApplicationContext().getString(R.string.sensor_ambient_temperature), "@drawable/ambient", R.drawable.ambient);

	private final String name;
	private final String image;
	private final int imageID;

	SensorsEnum(String name, String image, int imageID) {
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
