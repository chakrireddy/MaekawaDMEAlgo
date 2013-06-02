package ma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import message.Failed;
import message.Inquire;
import message.Release;
import message.Reply;
import message.Request;
import message.Yield;

public class MaekawaAlgorithm {
	Logger logger = Logger.getLogger("MaekawaAlgorithm.class");
	FileHandler fileHandler = null;
	File logfile = null;
	private static MaekawaAlgorithm maekawaAlgo = null;
	public static boolean start = false;
	private static int clock = 0;
	Node myNodeInfo = null;
	Node csNode = null;
	Node nodeZero = null;
	List<Node> nodeList = new ArrayList<Node>();
	int numberOfNodes = 0;
	int noTimesToCS = 0;
	int interRequestTime = 0;
	int csTime = 0;
	int capacity = 12;
	List<Node> quorum = new ArrayList<Node>();
	Hashtable<Integer, Client> clientTable = new Hashtable<Integer, Client>();
	Hashtable<Integer, Node> quorumTable = new Hashtable<Integer, Node>();
	HashMap<Integer,Boolean> terminationStatus = new HashMap<Integer,Boolean>();
	HashMap<Integer,Boolean> repliesStatus = new HashMap<Integer,Boolean>();
	Comparator<Request> comparator = new RequestComparator();
	PriorityBlockingQueue<Request> reqQueue= new PriorityBlockingQueue<Request>(capacity, comparator);
	List<MessageInfo> messageList = new ArrayList<MessageInfo>();
	File exeLogFile = null;
	FileWriter writer = null;
	boolean inCS = false;
	boolean granted = false;
	Request reqGrnt = null;
	int requestid=0;
	private MaekawaAlgorithm() {
		
	}
	
	public static synchronized MaekawaAlgorithm getInstance(){
		if(maekawaAlgo == null){
			maekawaAlgo = new MaekawaAlgorithm();
		}
		return maekawaAlgo;
	}
	
	public static void main(String[] args) {
		MaekawaAlgorithm maekawaAlgo = MaekawaAlgorithm.getInstance();
		maekawaAlgo.init(args);
		maekawaAlgo.readInput();
		if(!(Integer.parseInt(args[0])<0)){
			maekawaAlgo.initAlgo();
		}
	}
	
	public void init(String[] args){
		if(Integer.parseInt(args[0])>=0){
			logfile = new File("Log"+args[0]+".txt");
		}else {
			logfile = new File("Log.txt");
		}
		/*
		 * Read the config file and keep the nodes info
		 */
		Formatter format = new SimpleFormatter();
		try {
			fileHandler = new FileHandler(logfile.getAbsolutePath());
			fileHandler.setFormatter(format);
			logger.addHandler(fileHandler);
		} catch (SecurityException e1) {
			logger.log(Level.INFO, e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			logger.log(Level.INFO, e1.getMessage());
			e1.printStackTrace();
		}
		try {
			if(Integer.parseInt(args[0])>=0){
				exeLogFile = new File("ExecutionLog"+args[0]+".txt");
			}else {
				exeLogFile = new File("ExecutionLog.txt");
			}
			writer = new FileWriter(exeLogFile, true);
		} catch (FileNotFoundException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File configFile = new File("config.txt");
		BufferedReader buffFileReader = null;
		logger.log(Level.INFO, "reading the config file");
		try {
			buffFileReader = new BufferedReader(new FileReader(configFile));
			String info = buffFileReader.readLine();
			String[] csinfo = info.split(":");
			csNode = new Node(csinfo[0],Integer.parseInt(csinfo[1]),0);			
			String line = null;
			while ((line = buffFileReader.readLine()) != null) {
				String[] nodeInfo = line.split(":");
				if(Integer.parseInt(args[0])==Integer.parseInt(nodeInfo[0])){
//					myNodeInfo = new Node("localhost", Integer.parseInt(nodeInfo[2]),
					myNodeInfo = new Node(nodeInfo[1], Integer.parseInt(nodeInfo[2]),
							Integer.parseInt(nodeInfo[0]));					
					Server server = new Server(Integer.parseInt(nodeInfo[2]));
					server.start();
					System.out.println("myid: "+myNodeInfo.id+" host: "+myNodeInfo.host+" port: "+myNodeInfo.port);
				}else {
					Node nd = new Node(nodeInfo[1], Integer.parseInt(nodeInfo[2]),
						Integer.parseInt(nodeInfo[0]));
					System.out.println("id: "+nd.id+" host: "+nd.host+" port: "+nd.port);
					nodeList.add(nd);
					if(nd.id == 0){
						nodeZero = nd;
					}
				}
			}
			if(myNodeInfo == null){
				Server server = new Server(csNode.port);
				server.start();
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public void readInput(){
		/*
		 * Read the input file and build the quorum
		 */
		logger.log(Level.INFO, "reading the input file");
		File inputFile = new File("input.txt");
		BufferedReader buffFileReader = null;
		try {
			buffFileReader = new BufferedReader(new FileReader(inputFile));
			numberOfNodes = Integer.parseInt(buffFileReader.readLine().split("=")[1]);
			noTimesToCS = Integer.parseInt(buffFileReader.readLine().split("=")[1]);
			interRequestTime = Integer.parseInt(buffFileReader.readLine().split("=")[1]);
			csTime = Integer.parseInt(buffFileReader.readLine().split("=")[1]);
			String line = null;
			while ((line = buffFileReader.readLine()) != null && myNodeInfo != null) {
				String[] quorumInfo = line.split("=");
				//quorumInfo[0] contains like R0 and quorum[1] contains quorum like 0,1,2
				//check for the current node quorum
				int id = Integer.parseInt(quorumInfo[0].substring(1));
				if(id == myNodeInfo.id){
					logger.log(Level.INFO," this quorum contains "+quorumInfo[1]);
					//build quorum
					String quorumArr[] = quorumInfo[1].split(",");
					for (String string : quorumArr) {
						int quoNodeID = Integer.parseInt(string);
						for (Node node : nodeList) {
							if(quoNodeID == node.id){
								quorum.add(node);
								//make connections to the nodes in quorum
								Client client = new Client(node);
								//TODO reconsider the logic
								if(quoNodeID != myNodeInfo.id){
									client.connect();
									client.sendInfo();
								}
								//logger.log(Level.INFO, "***** clientid: "+node.id+" client: "+client);
								clientTable.put(node.id, client);
								quorumTable.put(node.id, node);
								terminationStatus.put(node.id, false);
								repliesStatus.put(node.id, false);
								
							}
						}
					}
					//TODO include mynodeinfo
					//clientTable.put(node.id, client);
					//logger.log(Level.INFO, "***** mynodeid: "+myNodeInfo.id+" node: "+myNodeInfo);
					quorumTable.put(myNodeInfo.id, myNodeInfo);
					terminationStatus.put(myNodeInfo.id, false);
					repliesStatus.put(id, false);
				}
			}
			
		} catch (FileNotFoundException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			e.printStackTrace();
		}
		//printDS();
		
	}
	
	public void initAlgo(){
		logger.log(Level.INFO, "Initializing algorithm");
		/*
		 * Run the algorithm
		 * If it is node 0 signal Start to other nodes and also indicate the termination
		 * collect logs of all nodes.
		 */
		if(myNodeInfo.id==0){
			for (Node node : nodeList) {
				//TODO creating unnecessary client connections to start
				Client client = new Client(node);
				client.connect();
				client.sendStart();
				client.close();
			}
			MaekawaAlgorithm.start = true;
		}
		
		while(MaekawaAlgorithm.start == false){
			Thread.yield();
		}
		
		runAlgo();
		if(myNodeInfo.id != 0){
			//TODO creating unnecessary client connections to terminate
			//send the termination message
			Client client = new Client(nodeZero);
			client.connect();
			client.sendTerminate();
			//send the execution log file
			client.sendLogFile(exeLogFile);
			client.close();
		}
		
		if(myNodeInfo.id == 0){
			boolean terminated = false;
			while(!terminated){
				int counter = 0;
				for (Object obj : terminationStatus.keySet()) {
					Integer integer = (Integer)obj;
					if(terminationStatus.get(integer)==true){
						counter++;
					}else{
						break;
					}
				}
				if(counter == nodeList.size()){
					terminated = true;
				}
			}
			logger.log(Level.INFO,"Application Terminated");
		}
		
	}
	
	/*private void printDS() {
		logger.log(Level.INFO, "--------  NodeList -------");
		for (Node node : nodeList) {
			logger.log(Level.INFO, "nodeid: "+node.id+" value: "+node);
		}
		
		logger.log(Level.INFO, "--------  QuorumList -------");
		for (Node node : quorum) {
			logger.log(Level.INFO, "nodeid: "+node.id+" value: "+node);
		}
		
		logger.log(Level.INFO, "--------  clientTable -------");
		for (int key : clientTable.keySet()) {
			logger.log(Level.INFO, "nodeid: "+key+" value: "+clientTable.get(key));
		}
	}*/

	public void runAlgo(){
		logger.log(Level.INFO,"Algorithm started to execute");
		Client csClient = new Client(csNode);
		csClient.connect();
		for (int i = 1; i <= noTimesToCS; i++) {
			MaekawaAlgorithm
			.setClock(MaekawaAlgorithm.getClock() + 1);
			requestid++;
			//TODO send the request to all nodes in the quorum.
			//TODO add the request to the queue
			reqQueue.add(new Request(myNodeInfo, requestid,MaekawaAlgorithm.getClock()));
			//TODO if the reply is granted then remove from the queue and update the replies table
			if(reqQueue.peek().node.id == myNodeInfo.id && !granted){
				//remove from queue update granted and update repliesstatus
				reqGrnt = reqQueue.poll();
				granted = true;
				repliesStatus.put(myNodeInfo.id, true);					
			}
			for (Integer nodeId : clientTable.keySet()) {
				if(nodeId == myNodeInfo.id){
					
				}else {
					Client client = clientTable.get(nodeId);
					client.sendRequest();
				}
			}
			//Check for the replies and enter into critical section.
			int replyCounter = 0;
			int quorumSize = quorumTable.size();
			while(replyCounter != quorumSize){
				replyCounter=0;
				for (Integer nodeid : repliesStatus.keySet()) {
					if(repliesStatus.get(nodeid)){
						replyCounter++;
					}
				}
			}
			//send message to enter into critical section
			//Client csClient = new Client(csNode);
			//csClient.connect();
			MaekawaAlgorithm
			.setClock(MaekawaAlgorithm.getClock() + 1);
			csClient.sendCSMessage();
			MessageInfo msgInfo = new MessageInfo(i, Request.counter, Reply.counter, Release.counter, Inquire.counter, Yield.counter, Failed.counter);
			messageList.add(msgInfo);
			logToFile(msgInfo.toString());
			//Check if it returned from critical section.
			while(inCS){
				Thread.yield();
			}
			//clear the counters
			resetCounters();
			//wait for sleep for IR seconds
			try {
				Thread.sleep(interRequestTime);
			} catch (InterruptedException e) {
				logger.log(Level.INFO, e.getMessage());
				e.printStackTrace();
			}
		}

	}
	
	public static int getClock() {
		return clock;
	}

	public synchronized static void setClock(int clock) {
		MaekawaAlgorithm.clock = clock;
	}
	
	public synchronized void logToFile(String msg){
		try {
			writer.write(msg);
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void resetCounters(){
		Request.counter = 0;
		Reply.counter = 0;
		Release.counter = 0;
		Failed.counter = 0;
		Inquire.counter = 0;
		Yield.counter = 0;
	}
}

class RequestComparator implements Comparator<Request>{

	@Override
	public int compare(Request req0, Request req1) {
		if(req0.requestID < req1.requestID){
			return 1;
		}else if(req0.requestID	> req1.requestID){
			return -1;
		}else{
			if(req0.node.id<req1.node.id){
				return 1;
			}else if(req0.node.id>req1.node.id){
				return -1;
			}
		}
		return 0;
	}
	
}
