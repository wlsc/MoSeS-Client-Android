package moses.client.test;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import moses.client.com.FakeResponseGenerator;

public class TestResponseGenerator implements FakeResponseGenerator {

	public static final String FAKE_NOTIFICATION_APK_ID = "66";
	
	private class Hardware {
		public String devid;
		public String sensors;
		public String andver;
		public String model_name;
		public String vendor_name;
	}

	private static Hardware hp;

	public static int sessionid = 0;
	private HashMap<String, Integer> m = null;

	public TestResponseGenerator() {
		m = new HashMap<String, Integer>();
		m.put("LOGIN_REQUEST", 0);
		m.put("GET_HARDWARE_PARAMS", 1);
		m.put("CHANGE_DEVICE_ID", 2);
		m.put("SET_HARDWARE_PARAMS", 3);
		m.put("GET_APK_INFO", 4);
	}

	@Override
	public String generateAnswer(JSONObject s) {
		if(s==null) {
			throw new NullPointerException("JSON object at Test response generator was null");
		}
		
		Log.d("TEST.RESPONSEGENERATOR", "Trying to generate an answer for request " + s.toString());
		try {
		//switch doesn't work wilt null argument, so we check if the message type was in the table before
		if(m.get(s.getString("MESSAGE"))==null) {
			return defaultAnswer(s);
		}
		
			switch (m.get(s.getString("MESSAGE"))) {
			case 0:
				return loginAnswer(s);
			case 1:
				return getHardwareParamsAnswer(s);
			case 2:
				return changeDeviceIDAnswer(s);
			case 3:
				return setHardwareParamsAnswer(s);
			case 4:
				return getApkInfoAnswer(s);
			default:
				return defaultAnswer(s);
			}
		} catch (JSONException e) {
			return defaultAnswer(s);
		}
	}

	private String getApkInfoAnswer(JSONObject s) throws JSONException {
		JSONObject a = new JSONObject();
		a.put("MESSAGE", "GET_APK_INFO_RESPONSE");
		a.put("STATUS", "SUCCESS");
		a.put("ID", TestResponseGenerator.FAKE_NOTIFICATION_APK_ID);
		a.put("NAME", "Notification Test App 1");
		a.put("DESCR", "Please install =)");
		Log.d("TEST.APKLISTREQUEST", "returning apk info request answer: "+a.toString());
		return a.toString();
	}

	private String defaultAnswer(JSONObject s) {
		return "";
	}

	private String loginAnswer(JSONObject s) throws JSONException {
		JSONObject a = new JSONObject();
		a.put("MESSAGE", "LOGIN_RESPONSE");
		if(s.getString("LOGIN").equals("alex") && s.getString("PASSWORD").equals("777")) {
			sessionid = (int) (Math.random()*10000000);
			a.put("SESSIONID", Integer.toString(sessionid));
			a.put("LOGIN", s.getString("LOGIN"));
		} else {
			a.put("SESSIONID", "NULL");
		}
		return a.toString();
	}

	private String getHardwareParamsAnswer(JSONObject s) throws JSONException {
		JSONObject a = new JSONObject();
		if(s.getString("SESSIONID").equals(Integer.toString(sessionid)) && s.getString("DEVICEID").equals(hp.devid)) {
			a.put("STATUS", "SUCCESS");
			a.put("DEVICEID", hp.devid);
			a.put("ANDVER", hp.andver);
			a.put("SENSORS", hp.sensors);
		} else {
			a.put("STATUS", "FAILURE");
		}
		return a.toString();
	}

	private String changeDeviceIDAnswer(JSONObject s) throws JSONException {
		JSONObject a = new JSONObject();
		if(s.getString("SESSIONID").equals(Integer.toString(sessionid))) {
			hp.devid = s.getString("DEVICEID");
		}
		return a.toString();
	}

	private String setHardwareParamsAnswer(JSONObject s) throws JSONException {
		JSONObject a = new JSONObject();
		a.put("MESSAGE", "HARDWARE_CHANGE_RESPONSE");
		if(s.getString("SESSIONID").equals(Integer.toString(sessionid))) {
			a.put("STATUS", "SUCCESS");
			hp.devid = s.getString("DEVICEID");
			hp.model_name = s.getString("MODEL_NAME");
			hp.vendor_name = s.getString("VENDOR_NAME");
			hp.andver = s.getString("ANDVER");
			hp.sensors = s.getString("SENSORS");
			a.put("MODEL_NAME", hp.model_name);
			a.put("VENDOR_NAME", hp.vendor_name);
			a.put("ANDVER", hp.andver);
		} else {
			a.put("STATUS", "FAILURE_INVALID_SESSION");
		}
		return a.toString();
	}
}
