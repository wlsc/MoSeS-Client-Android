package de.da_sense.moses.client.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;
import android.os.Environment;

/**
 * This class is used to give each device a unique ID. The ID is saved in two positions on
 * the device - the internal memory for the application in order to have a place where it can
 * be saved and not be tinkered with by the user. The second place is the external memory,
 * where it may be edited by the user, but survives updates from the application and reinstalls.
 * Unique IDs are variant 2, version 4 (randomly generated number) UUID as per RFC 4122. Collisions
 * may exist, but the possibility is extremely small.
 * 
 * @author Florian
 * 
 */
public class InstallationManager {
	private static String sID = null;
	private static final String INSTALLATION = "INSTALLATION";

	/**
	 * Returns the UUID. If none exists, it creates one.
	 * @param context
	 * @return
	 */
	public synchronized static String id(Context context) {
		if (sID == null) {
			int externalState = 0;
			String temp = getState();
			if(temp.compareTo("rw")== 0){
				externalState = 2;
			} else {
				if(temp.compareTo("r") == 0){
					externalState = 1;
				}					
			}
			File installationInternal = new File(context.getFilesDir(),
					INSTALLATION);
			File installationExternal = null;
			boolean intExists = installationInternal.exists();
			boolean extExists = false;
			if (externalState > 0){
				installationExternal = new File(
						Environment.getExternalStorageDirectory(), INSTALLATION);
				extExists = installationExternal.exists();
			} 
				try {
					if (!intExists) {
						if (!extExists) {
							String id = UUID.randomUUID().toString();
							writeInstallationFile(installationInternal, id);
							if (externalState == 2) writeInstallationFile(installationExternal, id);
						} else {
							String id = readInstallationFile(installationExternal);
							try {
								UUID.fromString(id);
								writeInstallationFile(installationInternal, id);
							} catch (NullPointerException e) {
								id = UUID.randomUUID().toString();
								writeInstallationFile(installationInternal, id);
								if (externalState == 2) writeInstallationFile(installationExternal, id);
							} catch (IllegalArgumentException e) {
								id = UUID.randomUUID().toString();
								writeInstallationFile(installationInternal, id);
								if (externalState == 2) writeInstallationFile(installationExternal, id);
							}
						}
					} else {
						String id = readInstallationFile(installationInternal);
						try {
							UUID.fromString(id);
							if (externalState == 2) writeInstallationFile(installationExternal, id);
						} catch (NullPointerException e) {
							id = UUID.randomUUID().toString();
							writeInstallationFile(installationInternal, id);
							if (externalState == 2) writeInstallationFile(installationExternal, id);
						} catch (IllegalArgumentException e) {
							id = UUID.randomUUID().toString();
							writeInstallationFile(installationInternal, id);
							if (externalState == 2) writeInstallationFile(installationExternal, id);
						}
					}
					sID = readInstallationFile(installationInternal);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
		}
		return sID;
	}

	/**
	 * Reads a file
	 * @param installation The file to read
	 * @return A string representation of the file's content.
	 * @throws IOException
	 */
	private static String readInstallationFile(File installation)
			throws IOException {
		RandomAccessFile f = new RandomAccessFile(installation, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	/**
	 * Write a file
	 * @param installation the file to write
	 * @param data the data to write
	 * @throws IOException
	 */
	private static void writeInstallationFile(File installation, String data)
			throws IOException {
		FileOutputStream out = new FileOutputStream(installation);
		out.write(data.getBytes());
		out.close();
	}

	/**
	 * Tristate if we can read/write, read or nothing at all.
	 * 
	 * @return "rw" if read/writeable, "r" if readable, but not writeable, "none" if neither read nor writeable.
	 */
	private static String getState() {
		String state = Environment.getExternalStorageState();
		String ret;

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			ret = "rw";
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			ret = "r";
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need to know is we can neither read nor write
			ret = "none";
		}
		return ret;
	}
}