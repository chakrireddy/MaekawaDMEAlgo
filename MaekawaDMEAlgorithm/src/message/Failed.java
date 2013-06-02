package message;

import java.io.Serializable;

public class Failed implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int counter = 0;
	public int timestamp = 0;
	public Failed(int timestamp) {
		//Failed.counter++;
		this.timestamp = timestamp;
	}
}
