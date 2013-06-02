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
		// TODO Auto-generated constructor stub
		this.timestamp = timestamp;
	}
}
