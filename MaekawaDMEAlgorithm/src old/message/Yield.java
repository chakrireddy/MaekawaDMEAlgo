package message;

import java.io.Serializable;

public class Yield implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int counter = 0;
	public int timestamp = 0;
	public Yield(int timestamp) {
		this.timestamp = timestamp;
	}
}
