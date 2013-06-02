package message;

import java.io.Serializable;

public class Ack implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int timestamp =0;
	public Ack(int timestamp) {
		// TODO Auto-generated constructor stub
		this.timestamp = timestamp;
	}
}
