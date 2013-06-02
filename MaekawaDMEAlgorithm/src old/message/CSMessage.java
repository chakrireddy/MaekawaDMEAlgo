package message;

import java.io.Serializable;

import ma.Node;

public class CSMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Node node = null;
	public int requestID = 0;
	public int cst = 0;
	public int timestamp = 0;
	public CSMessage(Node node, int requestID, int cst, int timestamp) {
		this.node = node;
		this.requestID = requestID;
		this.cst = cst;
		this.timestamp = timestamp;
	}
}
