package de.da_sense.moses.client.service.helpers;
/**
 * Class consisting of an EMessagType t and and Executable e
 * @author Jaco Hofmann
 *
 */
public class ExecutableWithType {

	/**
	 * Constructor for an Executable with an EMessageType.
	 * @param t the message type
	 * @param e the executable
	 */
	public ExecutableWithType(MessageTypesEnum t, Executable e) {
		this.e = e;
		this.t = t;
	}

	/** the executable */
	public Executable e;
	/** the message type */
	public MessageTypesEnum t;
}
