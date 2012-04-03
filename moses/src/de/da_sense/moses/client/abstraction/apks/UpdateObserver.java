package de.da_sense.moses.client.abstraction.apks;

public interface UpdateObserver {

	public void success(InstalledExternalApplication updatedApp);

	public void unsuccessful_exit();

	public void manual_abort();

}
