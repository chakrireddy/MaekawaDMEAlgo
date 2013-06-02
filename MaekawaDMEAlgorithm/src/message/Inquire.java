package message;

import java.io.Serializable;

public class Inquire implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int counter = 0;
	public int timestamp = 0;
	public Inquire(int timestamp) {
		//Inquire.counter++;
		this.timestamp = timestamp;
	}
}
