package program;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import utilities.BlockNum;
import utilities.packets.RequestPacket;
import utilities.simulator.Parameters;

public class Simulator4 {
	
	private static final int MAX_CAPACITY = 512;
	private static final int SIMULATOR_PORT = 29;
	
	private int serverPort;;
	private int clientPort;
	private RequestPacket requestPacket;
	private ArrayList<Integer> packetsToBeFailure;
	private ArrayList<Integer> packetsDone;
	private boolean packetFailure;
	private boolean isRead;	
	private boolean isWrite;	
	private boolean loseDataPacket;
	private boolean transferEnded;
	private int blockSent, blockReceived;
<<<<<<< HEAD
	private int lengthSent;
=======
	private int lengthSent, lengthReceived;
>>>>>>> master
	private Scanner scanner = new Scanner(System.in);
	
	
	private InetAddress clientAddress, serverAddress;
	
	// UDP datagram packets and sockets used to send / receive 
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket clientSocket, serverSocket;
		
	private Parameters parameters;
	
	//constructor
	public Simulator4() {
		//for it5 change the server Address here
		try {
			clientAddress = InetAddress.getLocalHost();
			serverAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
	public void listen() {
		int shutdown = -1;
				
		do {
			reset();
			parameters.getInfo();	
			
			loseDataPacket = (parameters.getPacketTypeID() == 1) ? true : false;
			
			saveFaileurePackets(parameters.getFrom(), parameters.getTo());
			
			transfer();
			
			System.out.println("\n");
			System.out.println("Transfer ended.");		
			System.out.println("\nType 1 to continue with another tranfer, type  0 to end");
			shutdown = scanner.nextInt();
			}while(shutdown == -1 ||shutdown == 1);
			
			if(shutdown == 0) System.out.println("Ending simulator");
			else System.out.println("wrong command, ending simulator");
			
			scanner.close();
			clientSocket.close();
			serverSocket.close();
	}
	

	public void reset() {
		if(clientSocket != null) clientSocket.close();
		if(serverSocket != null) serverSocket.close();
	
<<<<<<< HEAD
=======
		lengthReceived = 0;
>>>>>>> master
		lengthSent = 0;
		serverPort = 69;
		clientPort = -1;
		requestPacket = null;
		isRead = false;
		isWrite = true;
		packetsToBeFailure = new ArrayList<Integer>();
		packetsDone = new ArrayList<Integer>();
		packetFailure = false;
		loseDataPacket = false;
		transferEnded = false;
		blockSent = -1;
		blockReceived = -1;
		sendPacket = null;
		receivePacket = null;
		parameters = new Parameters();
		try {
			clientSocket = new DatagramSocket(SIMULATOR_PORT);
			serverSocket = new DatagramSocket();
			
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 
	}
			
	public void transfer() {	
			
		System.out.println("simulator listening on port 29...");
		int i = 0;
		
		boolean nonStablish= true;
		boolean temp = false;
<<<<<<< HEAD
=======
		boolean meesPacket = false;
>>>>>>> master
		while(!transferEnded) {
					

			//first parameter is used to identify if we are sending the request
			//second parameter is used to identify if we want to lose a dataPacket on write
			temp = fromClientToServerData(i, (isWrite && loseDataPacket) || (isRead && !loseDataPacket),  lengthSent < MAX_CAPACITY);
		
		
			//first parameter is used to identify if we are responding to a request
			//second parameter is used to identify if we want to lose a dataPacket on read
			fromServerToClientData(i, (isRead && loseDataPacket) || (isWrite && !loseDataPacket),  lengthSent < MAX_CAPACITY); 
			
			i++;
			
			if(temp) {
				nonStablish = false;
			}
			else if(i > 0 && nonStablish) {
				i = 0;
			}
			
		}		
			
			
		
	}
	
	//losing datapackets on write
	//losing ackpackets on read	
	public boolean fromClientToServerData(int i, boolean messPacket, boolean isEnd) {
		boolean conectionOk = false;
		
<<<<<<< HEAD
		if (isWrite || (isRead && !packetFailure)) {
=======
		if ((isWrite && loseDataPacket) || (isWrite && !loseDataPacket && !packetFailure) ||	
			(isRead && !loseDataPacket && !packetFailure) ||(isRead && loseDataPacket && !packetFailure) 
			
			) {
>>>>>>> master
			packetFailure = false;

			BlockNum pNumber = null;
			int operationID = parameters.getOperationID();		
			int callerID = 1;

<<<<<<< HEAD
			receivePacket(clientSocket);
=======
			System.out.println("Waiting for client to acknole agai");
			receivePacket(clientSocket);
			System.out.println("got fro client");
>>>>>>> master
			// request type and block num = 0
			if (i == 0) {
				try {
					requestPacket = new RequestPacket(receivePacket.getData(), receivePacket.getLength());
					isRead = (requestPacket.getID()[1] == 1) ? true : false;
					isWrite = !isRead;
					clientPort = receivePacket.getPort();
					pNumber = new BlockNum(0);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				byte[] temp = Arrays.copyOfRange(receivePacket.getData(), 2, 4);
				pNumber = new BlockNum(temp);
			}

			if (messPacket && packetsToBeFailure.contains(pNumber.getInt()) && !packetsDone.contains(pNumber.getInt()))
				doFailure(receivePacket, pNumber.getInt(), operationID, callerID);

			// Step 2: send to server (if i == 0 send to port 69 otherwise send to the
			// attending port)
			if (!packetFailure) {
				sendPacket(receivePacket, serverSocket, serverAddress, serverPort);
				if(isWrite) lengthSent = receivePacket.getLength();
				blockSent = pNumber.getInt();
				conectionOk = true;

				// how to end of transmission?
				if (isRead && i != 0 && isEnd && blockSent == blockReceived) {
					transferEnded = true;
				}
				
			}
		}

		return conectionOk;
	}

	//losing datapackets on read
	//losing ackpackets on write
	public void fromServerToClientData(int i, boolean messPacket,  boolean isEnd) {
	
<<<<<<< HEAD
		if (isRead || (isWrite && !packetFailure)) {
=======
		if ((isRead && loseDataPacket&& !transferEnded) || (isRead && !loseDataPacket) ||
			(isWrite && !loseDataPacket && !transferEnded) || (isWrite && loseDataPacket && !packetFailure) 	
			
			) {
>>>>>>> master

			packetFailure = false;
			int operationID = parameters.getOperationID();
			int callerID = 2;

			BlockNum pNumber = null;
			// Step 3 :
<<<<<<< HEAD
			receivePacket(serverSocket);
=======
			System.out.println("Waiting for server to give me data again");
			receivePacket(serverSocket);
			System.out.println("got data");
>>>>>>> master
			// change the port to the one that is attending our request;
			if (i == 0)
				serverPort = receivePacket.getPort();

			byte[] temp = Arrays.copyOfRange(receivePacket.getData(), 2, 4);
			pNumber = new BlockNum(temp);

			if (messPacket && packetsToBeFailure.contains(pNumber.getInt()) && !packetsDone.contains(pNumber.getInt()))
				doFailure(receivePacket, pNumber.getInt(), operationID, callerID);

			// step 4;
			if (!packetFailure) {
				sendPacket(receivePacket, clientSocket, clientAddress, clientPort);
				if(isRead) lengthSent = receivePacket.getLength();
				blockReceived = pNumber.getInt();
				// how to end transmission?
				if (!isRead && i != 0 && isEnd && blockSent == blockReceived) {
					transferEnded = true;
				}
				
				
			}

		}
	}
	
	
	public void failOnACKPackets() {
		
	}
	//returns true if packet was 
	private void doFailure(DatagramPacket dp, int packetNumber, int operationID, int callerID) {
		//System.out.println("Losing on "+ callerID +" side...");				
		if(operationID == 1) delayPacket(dp, callerID, packetNumber);
		else if (operationID == 2) duplicatePacket(dp, callerID, packetNumber);
		else if (operationID == 3) losePacket(dp, packetNumber);
		else if (operationID == 4) error4OpCode(dp, packetNumber);
		else if (operationID == 5) error4BlockNumber(dp, packetNumber);
		else if (operationID == 6) error5(dp, packetNumber, callerID);
		
		packetFailure = true;
		packetsDone.add(packetNumber);
	
	}
	
	private void receivePacket(DatagramSocket socket) {
		receivePacket = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);
		
		try {
			socket.receive(receivePacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void sendPacket(DatagramPacket dp,  DatagramSocket socket, InetAddress address, int port) {
		
		sendPacket = new DatagramPacket(dp.getData(), dp.getLength(), address, port);
		
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	
	//storages the packets that need a failure
	private void saveFaileurePackets(int from, int to) {
		for(int i = from; i<= to; i++) {
			packetsToBeFailure.add(i);
		}
	}
	
	
	//TODO: should we pass the delay?
	private void delayPacket(DatagramPacket dp, int id,  int pNumber) {
<<<<<<< HEAD
		byte[] opCode = Arrays.copyOfRange(dp.getData(), 0, 2);
=======
>>>>>>> master
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(id == 1) {
			sendPacket(dp,serverSocket, serverAddress, serverPort);
		}else {
			sendPacket(dp, clientSocket, clientAddress, clientPort);	
		}
		

		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
<<<<<<< HEAD
		System.out.println("Delaying " + temp2 + "Packet#" + pNumber + " on " + temp  + " Packet OPCODE IS :" + opCode[0] + " " + opCode[1]);
		
=======
		System.out.println("Delaying " + temp2 + "Packet#" + pNumber + " on " + temp);
>>>>>>> master

		
	}
	
	private void duplicatePacket(DatagramPacket dp, int id, int pNumber) {
<<<<<<< HEAD
		byte[] opCode = Arrays.copyOfRange(dp.getData(), 0, 2);
=======
>>>>>>> master
		if(id == 1) {
			sendPacket(dp, serverSocket, serverAddress, serverPort);
		}else {
			sendPacket(dp, clientSocket, clientAddress, clientPort);	
		}
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
<<<<<<< HEAD
		System.out.println("Duplicating " + temp2 + "Packet#" + pNumber + " on " + temp + " Packet OPCODE IS :" + opCode[0] + " " + opCode[1]);
=======
		System.out.println("Duplicating " + temp2 + "Packet#" + pNumber + " on " + temp);
>>>>>>> master

		
	}

	
	private void losePacket(DatagramPacket dp, int pNumber) {
<<<<<<< HEAD
		byte[] opCode = Arrays.copyOfRange(dp.getData(), 0, 2);
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Losing " + temp2 + "Packet#" + pNumber + " on " + temp + " Packet OPCODE IS :" + opCode[0] + " " + opCode[1]);
=======
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Losing " + temp2 + "Packet#" + pNumber + " on " + temp);
>>>>>>> master
	
	}
	
	
	//TODO : should we pass the new port?
	private void error5(DatagramPacket dp, int pNumber, int callerID) {
		
		InetAddress address = (callerID == 1) ? serverAddress : clientAddress;
		int port = (callerID == 1) ? serverPort : clientPort;
<<<<<<< HEAD
		byte[] opCode = Arrays.copyOfRange(dp.getData(), 0, 2);
=======
>>>>>>> master
		
		DatagramSocket temp = null;
		try {
			 temp = new DatagramSocket(28);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendPacket(dp, temp, address, port);
		receivePacket(temp);
		temp.close();

		
<<<<<<< HEAD
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp1 = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 5 " + temp2 + "Packet#" + pNumber + " on " + temp1 + " Packet OPCODE IS :" + opCode[0] + " " + opCode[1]);
=======
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp1 = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 5 " + temp2 + "Packet#" + pNumber + " on " + temp1);
>>>>>>> master
		
	}
	
	
	//TODO : should we pass the opcode?
	private void error4OpCode(DatagramPacket dp, int pNumber) {
		byte[] data = dp.getData();
<<<<<<< HEAD
		byte[] opCode = Arrays.copyOfRange(dp.getData(), 0, 2);
=======
>>>>>>> master
		
		data[0] = 99;
		data[1] = 99;		
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
<<<<<<< HEAD
		System.out.println("Generating ERROR 4 on OpCode " + temp2 + "Packet#" + pNumber + " on " + temp + " Packet OPCODE IS :" + opCode[0] + " " + opCode[1]);
=======
		System.out.println("Generating ERROR 4 on OpCode " + temp2 + "Packet#" + pNumber + " on " + temp);
>>>>>>> master
		
	
	}

	
	//TODO : should we pass the offset?
	private void error4BlockNumber(DatagramPacket dp, int pNumber) {
		byte[] data = dp.getData();
<<<<<<< HEAD
		byte[] opCode = Arrays.copyOfRange(dp.getData(), 0, 2);
		
		data[3] = (byte) (data[3] + 3);
		
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 4 on BlockNumber " + temp2 + "Packet#" + pNumber + " on " + temp + " Packet OPCODE IS :" + opCode[0] + " " + opCode[1]);
=======
		
		data[3] = (byte) (data[3] + 3);
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 4 on BlockNumber " + temp2 + "Packet#" + pNumber + " on " + temp);
>>>>>>> master
		
	
	}
	
	public static void main(String args[]) {
		Simulator4 simulator = new Simulator4();
		
		simulator.listen();
	}

}
