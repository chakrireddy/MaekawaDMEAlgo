package ma;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
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

public class Server extends Thread {

	int port = 0;
	ServerSocket serverSocket = null;
	MaekawaAlgorithm ma = null;
	Logger logger = Logger.getLogger("Server.class");
	FileHandler handler = null;
	Listener sockListener = null;

	public Server(int port) {
		this.port = port;
		this.ma = MaekawaAlgorithm.getInstance();
		logger.addHandler(ma.fileHandler);		
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.log(Level.INFO, ioe.getMessage());
			System.exit(1);
		}
		logger.log(Level.INFO, "server started on "+port);
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				
				Listener listener = new Listener(socket, this);
				this.sockListener = listener;
				listener.start();				
			} catch (IOException e) {
				e.printStackTrace();
				logger.log(Level.INFO, e.getMessage());
			}
		}
	}
}

class Listener extends Thread {
	Server server = null;
	Socket socket = null;
	ObjectInputStream ois = null;
	Node node = null;
	MaekawaAlgorithm ma = MaekawaAlgorithm.getInstance();

	public Listener(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		try {
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			server.logger.log(Level.INFO, e.getMessage());
			//TODO remove system exit code
			//System.exit(1);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Object obj = ois.readObject();
				if(obj instanceof Request){
					Request request = (Request)obj;
					server.logger.log(Level.INFO, "Received request from: "+request.node.id);
					if((request.requestID+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(request.requestID+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					if(!ma.granted){
						
						ma.reqGrnt = request;
						ma.granted = true;
						MaekawaAlgorithm
						.setClock(MaekawaAlgorithm.getClock() + 1);
						Client client = ma.clientTable.get(ma.reqGrnt.node.id);
						server.logger.log(Level.INFO, "id: "+ma.reqGrnt.node.id);
						client.sendReply();
					} else{
						ma.reqQueue.put(request);
						
						//TODO check once again
						MaekawaAlgorithm
						.setClock(MaekawaAlgorithm.getClock() + 1);
						int reqPriority = ma.comparator.compare(ma.reqGrnt, request);
						if(reqPriority == -1){
							
							//TODO if this node granted to itself check
							if(ma.reqGrnt.node.id == ma.myNodeInfo.id){
								int replyCounter = 0;
								int quorumSize = ma.quorumTable.size();
								if(replyCounter != quorumSize){
									replyCounter=0;
									for (Integer nodeid : ma.repliesStatus.keySet()) {
										if(ma.repliesStatus.get(nodeid)){
											replyCounter++;
										}
									}
								}
								if(replyCounter != quorumSize){
									//TODO code for yield
									
										Client client = ma.clientTable.get(request.node.id);
										client.sendReply();
									
									ma.reqQueue.put(ma.reqGrnt);
									ma.reqGrnt = request;
								}
							}else{
								//send Inquire nodeGrnt
								Client client = ma.clientTable.get(ma.reqGrnt.node.id);
								server.logger.log(Level.INFO, "id: "+ma.reqGrnt.node.id);
								server.logger.log(Level.INFO, "client: "+client);
								client.sendInquire();
							}
						}else{
							//send failed to node
							if(request.node.id == ma.myNodeInfo.id){
								ma.repliesStatus.put(request.node.id, false);
							}else {
								Client client = ma.clientTable.get(request.node.id);
								client.sendFailed();
							}
							
						}
					}
					//printState();
				}else if(obj instanceof Reply){
					Reply reply = (Reply)obj;
					Reply.counter++;
					server.logger.log(Level.INFO, "Received reply from node: "+node.id);
					if((reply.timestamp+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(reply.timestamp+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					//update reply status
					ma.repliesStatus.put(node.id, true);
					//printState();
				}else if(obj instanceof Failed){
					Failed failed = (Failed)obj;
					Failed.counter++;
					server.logger.log(Level.INFO, "Received failed from node: "+node.id);
					if((failed.timestamp+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(failed.timestamp+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					ma.repliesStatus.put(node.id, false);
					//printState();
				}else if(obj instanceof Inquire){
					Inquire inquire = (Inquire)obj;
					Inquire.counter++;
					server.logger.log(Level.INFO, "Received Inquire from node: "+node.id);
					if((inquire.timestamp+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(inquire.timestamp+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					//check for replies from other nodes and decide					
					int replyCounter = 0;
					int quorumSize = ma.quorumTable.size();
					if(replyCounter != quorumSize){
						replyCounter=0;
						for (Integer nodeid : ma.repliesStatus.keySet()) {
							if(ma.repliesStatus.get(nodeid)){
								replyCounter++;
							}
						}
					}
					if(replyCounter != quorumSize){
						MaekawaAlgorithm
						.setClock(MaekawaAlgorithm.getClock() + 1);
						//send yield
						Client client = ma.clientTable.get(node.id);
						client.sendYield();
					}
					//printState();
				}else if(obj instanceof Yield){
					Yield yield = (Yield)obj;
					server.logger.log(Level.INFO, "Received yield from node: "+node.id);
					if((yield.timestamp+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(yield.timestamp+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					// sendreply() to node and remove from queue
					// keep the yield node in the queue
					MaekawaAlgorithm
					.setClock(MaekawaAlgorithm.getClock() + 1);
					Request request = ma.reqQueue.poll();
					if(request.node.id == ma.myNodeInfo.id){
						ma.granted = true;
						ma.repliesStatus.put(request.node.id, true);
					}else {
						Client client = ma.clientTable.get(request.node.id);
						client.sendReply();
					}
					ma.reqQueue.put(ma.reqGrnt);
					ma.reqGrnt = request;
					//printState();
				}else if(obj instanceof Start){
					server.logger.log(Level.INFO, "Received start from node 0");
					MaekawaAlgorithm.start = true;
					//printState();
				}else if(obj instanceof Terminate){
					Terminate termination = (Terminate)obj;
					server.logger.log(Level.INFO, "Received termination from node: "+termination.node.id);
					server.ma.terminationStatus.put(termination.node.id, true);
					File logfolder = new File("Logs");
					if(!logfolder.exists()){
						logfolder.mkdir();
					}
					File logfile = new File(logfolder,"/ExecutionLog_"+termination.node.id+".txt");
					try {
						BufferedOutputStream buffOutStream = new BufferedOutputStream(new FileOutputStream(logfile));
						BufferedInputStream buffInsStream = new BufferedInputStream(socket.getInputStream());
						byte[] b = new byte[256];
						while (buffInsStream.read(b)!=-1) {
							buffOutStream.write(b);
							buffOutStream.flush();
						}
						buffOutStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					//printState();
				}else if(obj instanceof Info){
					Info info = (Info)obj;
					node = info.node;
					//printState();
				}else if(obj instanceof Ack){
					Ack ack = (Ack)obj;
					server.logger.log(Level.INFO, "Received ack from csnode ");
					if((ack.timestamp+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(ack.timestamp+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					//TODO update the replies status and send release to all quorum members including itself
					server.ma.inCS = false;					
					for (Integer nodeId : ma.clientTable.keySet()) {
						if(nodeId == ma.myNodeInfo.id){
							ma.granted = false;
						}else {
							MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
							Client client = ma.clientTable.get(nodeId);
							client.sendRelease();
						}
					}
					//printState();
				}else if(obj instanceof CSMessage){
					CSMessage csmsg = (CSMessage)obj;
					server.logger.log(Level.INFO, "Received CS msg from node: "+csmsg.node.id);
					if((csmsg.timestamp+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(csmsg.timestamp+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					Date starttime = new Date();
					server.logger.log(Level.INFO, csmsg.node.id+","+csmsg.requestID+","+starttime);
					server.ma.logToFile(csmsg.node.id+","+csmsg.requestID+","+starttime);
					try {
						Thread.sleep(csmsg.cst);
					} catch (InterruptedException e) {
						server.logger.log(Level.INFO, e.getMessage());
						e.printStackTrace();
					}
					Date endtime = new Date();
					server.logger.log(Level.INFO, csmsg.node.id+","+csmsg.requestID+","+endtime);
					server.ma.logToFile(csmsg.node.id+","+csmsg.requestID+","+endtime);
					MaekawaAlgorithm
					.setClock(MaekawaAlgorithm.getClock() + 1);
					Client client = new Client(csmsg.node);
					//Client client = new Client(ma.);
					client.connect();
					client.sendAck();
					client.close();
					//printState();
				}else if(obj instanceof Release){
					Release release = (Release)obj;
					server.logger.log(Level.INFO, "Received release from node: "+node.id);
					if((release.timestamp+1)>(MaekawaAlgorithm.getClock()+1)){
						//reset the clock
						MaekawaAlgorithm.setClock(release.timestamp+1);
					}else {
						MaekawaAlgorithm
							.setClock(MaekawaAlgorithm.getClock() + 1);
					}
					//
					if(ma.reqQueue.isEmpty()){
						ma.granted = false;
					}else {
						//TODO think again re initialization of hashmap
						for (Integer nodeid : ma.repliesStatus.keySet()) {
							ma.repliesStatus.put(nodeid, false);
						}
						ma.granted = true;
						MaekawaAlgorithm
						.setClock(MaekawaAlgorithm.getClock() + 1);
						Request request = ma.reqQueue.poll();
						if(request.node.id == ma.myNodeInfo.id){
							ma.repliesStatus.put(request.node.id, true);
						}else {
							Client client = ma.clientTable.get(request.node.id);
							client.sendReply();
						}
					}
					//printState();
				}
			} catch (IOException e) {
				//e.printStackTrace();
				//server.logger.log(Level.INFO, e.getMessage());
				closeConnection();
			} catch (ClassNotFoundException e) {
				//e.printStackTrace();
				//server.logger.log(Level.INFO, e.getMessage());
			}
		}
	}
	
	private void closeConnection() {
		try {
			ois.close();
		} catch (IOException e) {
		}
		try {
			socket.close();
		} catch (IOException e) {
		}
	}
	
	/*private void printState(){
		//Print the request queue
		server.logger.log(Level.INFO, "-------- Request Queue -------");
		for (Request request : ma.reqQueue) {
			server.logger.log(Level.INFO, request.toString());
		}
		//Print the granted node
		server.logger.log(Level.INFO, "-------- Request Granted -------");
		if(ma.reqGrnt != null){
			server.logger.log(Level.INFO, ma.reqGrnt.toString());
		}
		//Print the replies
		server.logger.log(Level.INFO, "-------- Replies Map -------");
		for (int key : ma.repliesStatus.keySet()) {
			server.logger.log(Level.INFO, "nodeid: "+key+" value: "+ma.repliesStatus.get(key));
		}
		//Print all other status message
		server.logger.log(Level.INFO, "-------- InCS: "+ma.inCS);
	}*/
}
