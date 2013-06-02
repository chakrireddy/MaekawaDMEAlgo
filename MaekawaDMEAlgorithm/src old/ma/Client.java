package ma;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import message.Ack;
import message.CSMessage;
import message.Failed;
import message.Info;
import message.Inquire;
import message.Release;
import message.Reply;
import message.Request;
import message.Start;
import message.Terminate;
import message.Yield;

public class Client{
	Socket socket = null;
	Node node = null;
	MaekawaAlgorithm ma = null;
	Logger logger = Logger.getLogger("Client.java");
	FileHandler handler = null;
	ObjectOutputStream oos = null;
	public Client(Node node) {
		this.node = node;
		this.ma = MaekawaAlgorithm.getInstance();
		logger.addHandler(ma.fileHandler);
	}
	
	public void connect() {
		while (socket == null) {
			try {
				socket = new Socket(node.host, node.port);				
			} catch (UnknownHostException e) {
				// e.printStackTrace();
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		logger.log(Level.INFO, "connection established with node: "+node.id);
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
		return;
	}
	
	public void sendRequest(){
		try {
			Request.counter++;
			logger.log(Level.INFO, "sending Request to node: "+node.id+" with requestid: "+ma.requestid);
			oos.writeObject(new Request(ma.myNodeInfo,ma.requestid,MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendReply(){
		try {
			Reply.counter++;
			logger.log(Level.INFO, "sending Reply to node: "+node.id);
			oos.writeObject(new Reply(MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendFailed(){
		try {
			Failed.counter++;
			logger.log(Level.INFO, "sending Failed to node: "+node.id);
			oos.writeObject(new Failed(MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendInquire(){
		try {
			Inquire.counter++;
			logger.log(Level.INFO, "sending Inquire to node: "+node.id);
			oos.writeObject(new Inquire(MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendYield(){
		try {
			Yield.counter++;
			logger.log(Level.INFO, "sending Yield to node: "+node.id);
			oos.writeObject(new Yield(MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendStart(){
		try {
			logger.log(Level.INFO, "sending signal START to node: "+node.id);
			oos.writeObject(new Start());
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendTerminate(){
		try {
			logger.log(Level.INFO, "sending Terminate to node: "+node.id);
			oos.writeObject(new Terminate(ma.myNodeInfo));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendInfo(){
		try {
			oos.writeObject(new Info(ma.myNodeInfo));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendCSMessage(){
		try {
			ma.inCS = true;
			logger.log(Level.INFO, "Entering into CS for "+ma.requestid+" time");
			oos.writeObject(new CSMessage(ma.myNodeInfo, 0, ma.csTime,MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendAck(){
		try {
			logger.log(Level.INFO, "sending Ack to node: "+node.id);
			oos.writeObject(new Ack(MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendRelease(){
		try {
			Release.counter++;
			logger.log(Level.INFO, "sending Release to node: "+node.id);
			oos.writeObject(new Release(MaekawaAlgorithm.getClock()));
			oos.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void sendLogFile(File file){
		try {
			BufferedOutputStream buffOutStream = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream buffInsStream = new BufferedInputStream(new FileInputStream(file));
			byte[] b = new byte[256];
			while (buffInsStream.read(b)!=-1) {
				buffOutStream.write(b);
				buffOutStream.flush();
			}
			buffInsStream.close();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			socket.close();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
	}
}
