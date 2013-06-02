package ma;

import java.io.Serializable;

public class Node implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String host = null;
	int port = 0;
	public int id = 0;
	public Node(String host, int port, int id) {
		this.host = host;
		this.port = port;
		this.id = id;
	}
}
