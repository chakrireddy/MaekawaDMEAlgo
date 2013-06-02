package message;

import java.io.Serializable;

import ma.Node;

public class Terminate implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Node node = null;
	public Terminate(Node node) {
		this.node = node;
	}
}
