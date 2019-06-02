//Simulator V2.0 based on simulator V1.0 and simulator from assignment 1;
package program;

import java.net.*;
import java.util.ArrayList;

import utilities.TFTPUtil;
import utilities.packets.*;
import utilities.simulator.Parameters;

public class Simulator2 {
	
	private static final boolean VERBOSE = true; //TODO: this field should be in the parameters class
	private static final int MAX_CAPACITY = 512;
	private static final int SERVER_PORT = 69;
	private static final int CLIENT_ID = 1;
	private static final int SERVER_ID = 2;
	
	// UDP datagram packets and sockets used to send / receive
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket clientSocket, serverSocket;
		
	//interface to get the parameters i.e. delay, duplicate, number of packets, etc
	private Parameters parameters;
	
	//stuff to keep track of requestType (to end this program)
	private RequestPacket requestPacket;
	private byte[] requestType;
	
	//array to keep track of packets that have been processed;
	private ArrayList<Integer> packetsProcessed = new ArrayList<Integer>();

	// constructor
	public Simulator2() {
		parameters = new Parameters();

		try {
			clientSocket = new DatagramSocket(29);
			serverSocket = new DatagramSocket();			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	// ''main method'' in this method user sets some fields on a simple user
	// interface then we go to the main sequence.
	private void listen() {
		// shows interface
		showInterface();

		mainSequence();
	}

	private void mainSequence() {
		
		int clientPort = -1;
		int serverPort = SERVER_PORT;		
		//sizes to know when to stop
		int sendSize = MAX_CAPACITY;
		int receivedSize = MAX_CAPACITY;		
		int packetCounter = 0;
		
		while(true) {		
			
			//step 1 : get a packet from the client	
			//(if it is the first time we loop, we receive	the request, else we receive a Data/ACK packet)	
			receivePacket(clientSocket, "");
			
			
			//check the size to terminate when size < 512 and request == write
			sendSize = receivePacket.getLength();
									
			//keep a handle on requestPacket to know when to stop
			if(packetCounter == 0) {				
				requestPacket = new RequestPacket(receivePacket.getData(), receivePacket.getLength());
				requestType = requestPacket.getID();
				//change clients port 
				clientPort = receivePacket.getPort();
				sendSize = MAX_CAPACITY;
			}
			
			
			//Step 2 : send packet to server
			//at this moment client is waiting response, packet could be lost/duplicated/etc (it is done inside sendPacket)
			sendPacket(serverSocket, serverPort, CLIENT_ID, "step 2");
			
			
			
			//end transaction when size of packet is less than Max_Capacity 
			if ((requestType[1] == 1 && receivedSize < MAX_CAPACITY) || (requestType[1] == 2 && sendSize < MAX_CAPACITY)){				
				if (VERBOSE) System.out.println("end of transaction");
				break;
			}
					
			
			//Step 3 : receivePacket from server, can be and ACK or DATA packet depending on request type
			receivePacket(serverSocket,"step 3");
			
					
			//check the size to terminate when size < 512 and when request == READ
			receivedSize = receivePacket.getLength();
			
			//Step 4: sendPacket to the client	
			//at this moment server waits for response, packet could be lost/duplicated/etc
			sendPacket(clientSocket, clientPort, SERVER_ID, "step 4");
			
			//change the portServer to the one that is attending our request			
			serverPort = receivePacket.getPort();				
			packetCounter++;								
		}
		 
	}

	//method to send packets
	private void sendPacket(DatagramSocket socket, int port, int callerID,  String msj) {
		//set a datagram used to send information
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), port);
		
		//if caller ID and parameters.whoID are equal means that we want to perfom an operation
		if (callerID == parameters.getWhoID()) {
			Integer opID = manipulatePacket(socket, sendPacket);
			
			//do operation returns null to indicate loosing a packet
			//to loose a packet we just don't send it (we return)
			if(opID == null) return;
		}
		
		//send packet
		TFTPUtil.send(socket, sendPacket, msj/*, VERBOSE*/);
	}

	//method to receive packets
	private void receivePacket(DatagramSocket socket, String msj) {
		receivePacket = TFTPUtil.datagramPacket(MAX_CAPACITY);

		TFTPUtil.receive(socket, receivePacket, msj, VERBOSE);
	}

	//method to show interface
	public void showInterface() {
		parameters.getInfo();

		if (VERBOSE) {
			String failSide= (parameters.getWhoID() == CLIENT_ID) ? " (on client side)" : " (on server side)";
			System.out.println("Packet #" + parameters.getFrom() + " to #" + parameters.getTo()	+ " will " + parameters.getOperationName() + failSide);
			System.out.println("Simulator:Listening on Port 29");
		}

	}

	//method that does the operation selected on the interface (delay, duplicate, lost).
	//once a packet is processed (delayed, duplicated, etc),
	//that packet gets stored in an ArrayList to keep track of them and no repeat; 
	private Integer manipulatePacket(DatagramSocket socket, DatagramPacket packet) {
		int whoID = parameters.getWhoID();
		int operationID = parameters.getOperationID();
		String operationName = parameters.getOperationName();
		

		int packetNumber = fetchBlockNumber(packet, whoID);

		if (!packetsProcessed.contains(packetNumber) && packetNumber >= parameters.getFrom() && packetNumber <= parameters.getTo()) {

			packetsProcessed.add(packetNumber);
			
			if (VERBOSE) {
				String failSide= (whoID == CLIENT_ID) ? " (on client side)" : " (on server side)";
				System.out.println("Doing " + operationName + " on packet " + packetNumber + failSide);
			}

			//delay packet
			if (operationID == 1) {
				try {
					Thread.sleep(1500); // TODO: this value should be defined on the GUI
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			//duplicate packet
			} else if (operationID == 2) {
				TFTPUtil.send(socket, packet, ""/*, VERBOSE*/);
				
			//lose packet
			} else if (operationID == 3) {
				
				return null;
			}			
		}
		
		return operationID;
	}

	//method used to get the packet number in the manipulatePacket method;
	private int fetchBlockNumber(DatagramPacket packet, int whoID) {
		int packetNumber = -1;

		// if client starts loosing
		if (whoID == 1) {
			// and the request is a read request, we lose dataPackets
			if (requestType[1] == 1) {
				DataPacket tempPacket = new DataPacket(packet.getData(), packet.getLength());
				packetNumber = tempPacket.getIntBN();
			}
			// or if request is a write request, we lose ackPackets
			else if (requestType[1] == 2) {
				ACKPacket tempPacket = new ACKPacket(packet.getData(), packet.getLength());
				packetNumber = tempPacket.getIntBN();
			}
			
			//if server starts loosing
		} else if (whoID == 2) {
			if (requestType[1] == 1) {
				ACKPacket tempPacket = new ACKPacket(packet.getData(), packet.getLength());
				packetNumber = tempPacket.getIntBN();
			} else if (requestType[1] == 2) {
				DataPacket tempPacket = new DataPacket(packet.getData(), packet.getLength());
				packetNumber = tempPacket.getIntBN();
			}
		}
		return packetNumber;
	}

	public static void main(String arg[]) {

		Simulator2 simulator = new Simulator2();
		simulator.listen();

	}

}