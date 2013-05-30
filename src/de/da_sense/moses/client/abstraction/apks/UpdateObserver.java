package de.da_sense.moses.client.abstraction.apks;

/**
 * Interface for an Observer. Contains 3 Methods: Success, unsuccessful_exit and manual_abort.
 * @author Florian
 *
 */
public interface UpdateObserver {

	public void success(InstalledExternalApplication updatedApp);

	public void unsuccessful_exit();

	public void manual_abort();

}
