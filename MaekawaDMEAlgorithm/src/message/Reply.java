package message;

import java.io.Serializable;

public class Reply implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int counter = 0;
	public int timestamp = 0;
	public Reply(int timestamp) {
		//Reply.counter++;
		this.timestamp = timestamp;
	}

}
