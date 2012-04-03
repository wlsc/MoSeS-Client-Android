package de.da_sense.moses.client.service.helpers;
/**
 * 
 * @author Jaco Hofmann
 *
 */
public class ExecutorWithType {

	public ExecutorWithType(EMessageTypes t, Executor e) {
		this.e = e;
		this.t = t;
	}

	public Executor e;
	public EMessageTypes t;
}
