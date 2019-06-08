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
import utilities.packets.ACKPacket;
import utilities.packets.DataPacket;
import utilities.packets.RequestPacket;
import utilities.simulator.Parameters;

public class Simulator3 {
	
	private static final int MAX_CAPACITY = 512;
	private static final int SIMULATOR_PORT = 29;
	
	private int serverPort;;
	private int clientPort;
	private RequestPacket requestPacket;
	private ArrayList<Integer> packetsToBeFailure;
	private ArrayList<Integer> packetsDone;
	private boolean packetLost;
	private boolean isRead;	
	private boolean loseDataPacket;
	private boolean transferEnded;
	private int blockSent, blockReceived;
	private Scanner scanner = new Scanner(System.in);
	
	private InetAddress clientAddress, serverAddress;
	
	// UDP datagram packets and sockets used to send / receive 
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket clientSocket, serverSocket;
		
	private Parameters parameters;
	
	//constructor
	public Simulator3() {
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
			
			saveFaileurePackets();
			
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
		serverPort = 69;
		clientPort = -1;
		requestPacket = null;
		isRead = false;
		packetsToBeFailure = new ArrayList<Integer>();
		packetsDone = new ArrayList<Integer>();
		packetLost = false;
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
		int lengthSent = 0;
		int lengthReceived = 0;
		boolean nonStablish= true;
		boolean temp = false;
		while(!transferEnded) {
		
			if(nonStablish && !temp && i > 0) {
				nonStablish = false;
				i = 0;
			}
			//first parameter is used to identify if we are sending the request
			//second parameter is used to identify if we want to lose a dataPacket on write
			//third parameter is used to identify if we want to lose an ackpacket on write;
			temp = fromClientToServer(i, !isRead && loseDataPacket, isRead && !loseDataPacket, lengthReceived < MAX_CAPACITY);
			lengthSent = sendPacket.getLength();
			//first parameter is used to identify if we are responding to a request
			//second parameter is used to identify if we want to lose a dataPacket on read
			//third parameter is used to identify if we want to lose an ackpacket on read;
			fromServerToClient(i, isRead && loseDataPacket, !isRead && loseDataPacket, lengthSent < MAX_CAPACITY); 
			lengthReceived = receivePacket.getLength();
					
			i++;
		}
		
	}
	
	
	public boolean fromClientToServer(int i, boolean messDataPacket, boolean messACKPacket, boolean isEnd) {
		boolean packetSafe = false;
		
		if ((!isRead && messDataPacket) || (isRead && messACKPacket) || !packetLost) {

			packetLost = false;
		

			BlockNum pNumber = null;
			int operationID = parameters.getOperationID();
			int callerID = 1;

			receivePacket(clientSocket);

			// request type and block num = 0
			if (i == 0) {
				try {
					requestPacket = new RequestPacket(receivePacket.getData(), receivePacket.getLength());
					isRead = (requestPacket.getID()[1] == 1) ? true : false;
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

			if (messDataPacket && packetsToBeFailure.contains(pNumber.getInt())
					&& !packetsDone.contains(pNumber.getInt()))
				doFailure(pNumber.getInt(), operationID, callerID);

			// Step 2: send to server (if i == 0 send to port 69 otherwise send to the
			// attending port)
			if (!packetLost) {
				sendPacket(serverSocket, serverAddress, serverPort);
				packetSafe = true;
			}
			
			
			blockSent = pNumber.getInt();

			// end of transmission?
			if (isRead && i != 0 && isEnd) {
				transferEnded = true;
			}
		}
		
		return packetSafe;
	}
	
	
	//no quiero que entre aqui si es un  write queremos messdatapackets y el packet is lost
	
	//pero si debe entrar si es un read y queremos  messackpackets
	public void fromServerToClient(int i, boolean messDataPacket, boolean messACKPacket, boolean isEnd) {
		if ((isRead && messDataPacket) || (!isRead && messACKPacket) || !packetLost) {
			packetLost = false;
			boolean packetSafe = false;

			int operationID = parameters.getOperationID();

			int callerID = 2;

			BlockNum pNumber = null;
			// Step 3 :
			receivePacket(serverSocket);

			// change the port to the one that is attending our request;
			if (i == 0)
				serverPort = receivePacket.getPort();

			byte[] temp = Arrays.copyOfRange(receivePacket.getData(), 2, 4);
			pNumber = new BlockNum(temp);
			blockReceived = pNumber.getInt();

			if (messDataPacket && packetsToBeFailure.contains(pNumber.getInt())
					&& !packetsDone.contains(pNumber.getInt()))
				doFailure(pNumber.getInt(), operationID, callerID);

			// step 4;
			if (!packetLost)
				sendPacket(clientSocket, clientAddress, clientPort);

			// end of transmission?
			if (!isRead && i != 0 && isEnd && blockSent == blockReceived) {
				transferEnded = true;

			}
		}
	}
	
	//returns true if packet was 
	private void doFailure(int packetNumber, int operationID, int callerID) {
		//System.out.println("Losing on "+ callerID +" side...");				
		if(operationID == 1) delayPacket(packetNumber);
		else if (operationID == 2) duplicatePacket(callerID, packetNumber);
		else if (operationID == 3) losePacket(packetNumber);
		else if (operationID == 4) error4OpCode(packetNumber);
		else if (operationID == 5) error4BlockNumber(packetNumber);
		else if (operationID == 6) error5(packetNumber, callerID);
		
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
	
	private void sendPacket(DatagramSocket socket, InetAddress address, int port) {
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), address, port);
		
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	
	//storages the packets that need a failure
	private void saveFaileurePackets() {
		for(int i = parameters.getFrom(); i<= parameters.getTo(); i++) {
			packetsToBeFailure.add(i);
		}
	}
	
	

	// TODO: should we pass the delay?
	private void delayPacket(int pNumber) {
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Delaying " + temp2 + "Packet#" + pNumber + " on " + temp);

		packetLost = false;
	}

	private void duplicatePacket(int id, int pNumber) {
		if (id == 1) {
			sendPacket(serverSocket, serverAddress, serverPort);
		} else {
			sendPacket(clientSocket, clientAddress, clientPort);
		}

		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Duplicating " + temp2 + "Packet#" + pNumber + " on " + temp);

		packetLost = false;
	}

	private void losePacket(int pNumber) {
		packetLost = true;

		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Losing " + temp2 + "Packet#" + pNumber + " on " + temp);
	}

	// TODO : should we pass the new port?
	private void error5(int pNumber, int callerID) {
		System.out.println(callerID);
		InetAddress address = (callerID == 1) ? serverAddress : clientAddress;
		int port = (callerID == 1) ? serverPort : clientPort;

		DatagramSocket temp = null;
		try {
			temp = new DatagramSocket(28);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendPacket(temp, address, port);
		receivePacket(temp);
		temp.close();
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp1 = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 5 " + temp2 + "Packet#" + pNumber + " on " + temp1);
		packetLost = false;
	}

	// TODO : should we pass the opcode?
	private void error4OpCode(int pNumber) {
		byte[] data = receivePacket.getData();

		data[0] = 99;
		data[1] = 99;
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 4 on OpCode " + temp2 + "Packet#" + pNumber + " on " + temp);
		packetLost = false;
	}

	// TODO : should we pass the offset?
	private void error4BlockNumber(int pNumber) {
		byte[] data = receivePacket.getData();

		data[3] = (byte) (data[3] + 3);

		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 4 on BlockNumber " + temp2 + "Packet#" + pNumber + " on " + temp);
		packetLost = false;
	}

	public static void main(String args[]) {
		Simulator3 simulator = new Simulator3();

		simulator.listen();
	}
}