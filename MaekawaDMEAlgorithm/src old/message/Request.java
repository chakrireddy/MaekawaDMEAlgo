package message;

import java.io.Serializable;

import ma.Node;

public class Request implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int counter = 0;
	public Node node = null;
	public int requestID = 0;
	public int timestamp = 0;
	public Request(Node node, int requestID, int timestamp) {
		this.node = node;
		this.requestID = requestID;
		this.timestamp = timestamp;
	}
}
