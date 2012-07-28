package de.da_sense.moses.client.service.helpers;
/**
 * Class consisting of an EMessagType t and and Executable e
 * @author Jaco Hofmann
 *
 */
public class ExecutableWithType {

	public ExecutableWithType(EMessageTypes t, Executable e) {
		this.e = e;
		this.t = t;
	}

	public Executable e;
	public EMessageTypes t;
}
