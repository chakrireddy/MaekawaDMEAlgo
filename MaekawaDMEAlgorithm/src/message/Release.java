package message;

import java.io.Serializable;

public class Release implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int counter = 0;
	public int timestamp = 0;
	public Release(int timestamp) {
		Request.counter++;
		this.timestamp = timestamp;
	}
}
