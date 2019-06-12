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
import utilities.packets.ErrorPacket;
import utilities.packets.RequestPacket;
import utilities.simulator.Parameters;

public class Simulator {
	
	private static final int MAX_CAPACITY = 512;
	private static final int SIMULATOR_PORT = 29;
	private  InetAddress SERVER_ADDRESS =null;
	private   int SERVER_PORT = 69;
	
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
	private int lengthSent;
	private Scanner scanner = new Scanner(System.in);
	private boolean endByError;
	private boolean isError;
	
	private InetAddress clientAddress, serverAddress;
	
	// UDP datagram packets and sockets used to send / receive 
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket clientSocket, serverSocket;
		
	private Parameters parameters;
	
	//constructor
	public Simulator() {
		//for it5 change the server Address here
		serverPort = SERVER_PORT;
		try {
			SERVER_ADDRESS = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		serverAddress= SERVER_ADDRESS;
		
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
	
		isError = false;
		endByError = false;
		serverAddress = SERVER_ADDRESS;
		serverPort = SERVER_PORT;		
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
		while(!transferEnded) {
					

			//first parameter is used to identify if we are sending the request
			//second parameter is used to identify if we want to lose a dataPacket on write
			temp = fromClientToServerData(i, (isWrite && loseDataPacket) || (isRead && !loseDataPacket),  lengthSent < MAX_CAPACITY);
		
			if(endByError) break;
			if(transferEnded) break;
			//first parameter is used to identify if we are responding to a request
			//second parameter is used to identify if we want to lose a dataPacket on read
			fromServerToClientData(i, (isRead && loseDataPacket) || (isWrite && !loseDataPacket),  lengthSent < MAX_CAPACITY); 
			
			if(endByError) break;
			
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
		
		if (isWrite || (isRead && !packetFailure)) {
			packetFailure = false;

			BlockNum pNumber = null;
			int operationID = parameters.getOperationID();		
			int callerID = 1;

		
			receivePacket(clientSocket);
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
			if(endByError) return false;
			
			if (!packetFailure) {
				sendPacket(receivePacket, serverSocket, serverAddress, serverPort);
				checkForErrorPacket();
				if(isError) {
					endByError = true;
					return false;
				}
				if(isWrite) lengthSent = receivePacket.getLength();
				blockSent = pNumber.getInt();
				conectionOk = true;

				// how to end of transmission?
				boolean temp =   blockSent == blockReceived;
				if (isRead && i != 0 && isEnd && temp) {					
					transferEnded = true;
				}
				
				
			}
		}

		return conectionOk;
	}

	//losing datapackets on read
	//losing ackpackets on write
	public void fromServerToClientData(int i, boolean messPacket,  boolean isEnd) {
	
		if (isRead || (isWrite && !packetFailure)) {

			packetFailure = false;
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

			if (messPacket && packetsToBeFailure.contains(pNumber.getInt()) && !packetsDone.contains(pNumber.getInt()))
				doFailure(receivePacket, pNumber.getInt(), operationID, callerID);

			// step 4;
			if(endByError) return;
			if (!packetFailure) {
				sendPacket(receivePacket, clientSocket, clientAddress, clientPort);
				checkForErrorPacket();
				if(isError) {
					endByError = true;
					return;
				}
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
		else if (operationID == 4) error4OpCode(dp, packetNumber, callerID);
		else if (operationID == 5) error4BlockNumber(dp, packetNumber, callerID);
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
		System.out.println("Delaying " + temp2 + "Packet#" + pNumber + " on " + temp);

		
	}
	
	private void duplicatePacket(DatagramPacket dp, int id, int pNumber) {
		if(id == 1) {
			sendPacket(dp, serverSocket, serverAddress, serverPort);
		}else {
			sendPacket(dp, clientSocket, clientAddress, clientPort);			
		}
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Duplicating " + temp2 + "Packet#" + pNumber + " on " + temp);

		
	}

	
	private void losePacket(DatagramPacket dp, int pNumber) {
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Losing " + temp2 + "Packet#" + pNumber + " on " + temp);
	
	}
	
	
	//TODO : should we pass the new port?
	private void error5(DatagramPacket dp, int pNumber, int callerID) {
		
		
		
		DatagramSocket temp = null;
		try {
			 temp = new DatagramSocket(28);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(callerID == 1) {
						
			sendPacket(dp, temp, serverAddress, serverPort);
			
			receivePacket(temp);
						
			sendPacket(dp, serverSocket, serverAddress, serverPort);
			
			
		}else {
			sendPacket(dp, temp, clientAddress, clientPort);	
			
			receivePacket(temp);
			
			sendPacket(dp, clientSocket, clientAddress, clientPort);
						
		}
		temp.close();
				
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp1 = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 5 " + temp2 + "Packet#" + pNumber + " on " + temp1);
		
		ErrorPacket errorPacket = new ErrorPacket(receivePacket.getData(), receivePacket.getLength());
		
		System.out.println("GOT ERROR PACKET with the following code:" + errorPacket.getIntBN() +  " and msg: " + errorPacket.getMsg());
		System.out.println("");
	}
	
	
	//TODO : should we pass the opcode?
	private void error4OpCode(DatagramPacket dp, int pNumber, int callerID) {
		byte[] data = dp.getData();
			
		data[0] = 99;
		data[1] = 99;		
		
		DatagramPacket tempData = new DatagramPacket(data, dp.getLength(), dp.getAddress(), dp.getPort());
		
		
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 4 on OpCode " + temp2 + "Packet#" + pNumber + " on " + temp);
		
		if(callerID == 1) {
			sendPacket(tempData, serverSocket, serverAddress, serverPort);
			
			receivePacket(serverSocket);
			
			sendPacket(receivePacket, clientSocket, clientAddress, clientPort);
			
			endByError = true;
			
		}else {
			sendPacket(tempData, clientSocket, clientAddress, clientPort);	
			
			receivePacket(clientSocket);
			
			sendPacket(receivePacket, serverSocket, serverAddress, serverPort);
			
			endByError = true;
		}
				
		System.out.println("END BY ERROR 4 ON OPCODE");
		
	}
	
	public void checkForErrorPacket() {
		byte[] temp = Arrays.copyOfRange(receivePacket.getData(), 0, 2);
		
		if(temp[0] == 0 && temp[1] == 5) {
			isError = true;
		}
		
	}

	
	//TODO : should we pass the offset?
	private void error4BlockNumber(DatagramPacket dp, int pNumber, int callerID) {
		byte[] data = dp.getData();
		
		//TODO CHANGE THIS TO  NEGATIVE VALUES
		data[2] = (byte) 0b11111111;
		data[3] = (byte)  0b11111111;
		
		DatagramPacket tempData = new DatagramPacket(data, dp.getLength(), dp.getAddress(), dp.getPort());
		
		
		String temp2 = (parameters.getPacketTypeID() == 1) ? "Data" : "ACK";
		String temp = (isRead) ? "Read" : "Write";
		System.out.println("Generating ERROR 4 on BlockNumber " + temp2 + "Packet#" + pNumber + " on " + temp);
		
		if(callerID == 1) {
			sendPacket(tempData, serverSocket, serverAddress, serverPort);
			
			receivePacket(serverSocket);
			
			sendPacket(receivePacket, clientSocket, clientAddress, clientPort);
			
			endByError = true;
			
		}else {
			sendPacket(tempData, clientSocket, clientAddress, clientPort);	
			
			receivePacket(clientSocket);
			
			sendPacket(receivePacket, serverSocket, serverAddress, serverPort);
			
			endByError = true;
		}
		
		
		System.out.println("END BY ERROR 4 ON BLOCK NUMBER");
		
	}
	
	public static void main(String args[]) {
		Simulator simulator = new Simulator();
		
		simulator.listen();
	}

}
