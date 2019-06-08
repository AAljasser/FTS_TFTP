package utilities.client;

import java.io.*;
import java.net.*;
import java.util.*;
import utilities.*;
import utilities.packets.*;

public class Clientaso {

	private static final String PATH = "C:\\Jose\\Java\\files\\";///
	private static final boolean VERBOSE = true;//
	private static final int MAX_CAPACITY = 512;///
	private static final int SERVER_PORT = 69;///

	private int serverPort;///
	private InetAddress serverAddress;///
	private DatagramSocket sendReceiveSocket;///
	private DatagramPacket sendPacket, receivePacket;///

	private RequestPacket requestPacket;
	private Integer originalPort;///
	private boolean isRead;///	
	private byte[][] data;///
	

	private int blockToSend;

	private int counter = 0;
	private int blockReceived;
	private ArrayList<Integer> packetsReceived;
	private Integer blockExpected;
	private Integer itNum;
	private boolean received;
	
	private Scanner scanner = new Scanner(System.in);

	public Clientaso() {
		try {
			serverAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendReceive() {
		String shutdown = null;
		
		do {
			reset();		
			requestPacket = showInterface();	
			
			transfer();
	
			System.out.println("\n");
			System.out.println("Transfer ended.");
			System.out.println("\nType 1 to continue with another tranfer, type  0 to end");
		
			shutdown = scanner.nextLine();
		}
		while(shutdown == null || shutdown.contentEquals("1"));
		if(shutdown.equals("0")) endClient("Ending client by command");
		else endClient("Ending client wrong command");
		
		System.out.println("");
		
	}
	private void reset() {
		if(sendReceiveSocket != null) sendReceiveSocket.close();
		
		serverPort = SERVER_PORT;
		sendPacket = null;
		receivePacket = null;
		originalPort = null;
		data = new byte[1024][];
		
		packetsReceived = new ArrayList<Integer>();		
		blockReceived = -1;
		blockExpected = 0;
		blockToSend = 0;
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void transfer() {

		itNum = 0;
		received = false;
		boolean transferEnded = false;
		Packet packetToSend = requestPacket;
		
		isRead = requestPacket.getID()[1] == 1;
		data = (isRead) ? data : getFile();

		do {
			sendPacket(packetToSend);
			transferEnded = receivePacket();

			if (received) {
				checkReceivePacket();
				
				if(isRead) {
					packetToSend = new ACKPacket(blockReceived);
				}
				else {
					packetToSend = new DataPacket(blockExpected, data[blockReceived]);
				}
			}

			if (blockReceived == 0) {
				serverPort = receivePacket.getPort();
				originalPort = serverPort;
				serverAddress = receivePacket.getAddress();
			}

		} while (!transferEnded);
	}

	private void checkForCorruptionError() {

		BlockNum actualPacket = new BlockNum(Arrays.copyOfRange(receivePacket.getData(), 2, 4));

		byte[] expectedID = (isRead) ? new byte[] { 0, 3 } : new byte[] { 0, 4 };

		byte[] errorID = new byte[] { 0, 5 };

		byte[] actualID = Arrays.copyOfRange(receivePacket.getData(), 0, 2);

		if (actualID[0] == errorID[0] && actualID[1] == errorID[1]) {
			ErrorPacket errorPacket = new ErrorPacket(receivePacket.getData(), receivePacket.getLength());

			String msg = (errorPacket.getMsg().isEmpty()) ? "" : "(" + errorPacket.getMsg() + " )";

			endClient("Ending client, Error #" + errorPacket.getIntBN() + msg);
		}

		if (expectedID[0] != actualID[0] || expectedID[1] != actualID[1] || blockExpected < actualPacket.getInt()) {
			ErrorPacket errorPacket = new ErrorPacket(4, "Illegal TFTP operation");

			sendPacket(errorPacket);

			endClient("Ending client  Error 4 (Illegal TFTP operation)");
		}
	}

	private void checkReceivePacket() {
		if (isRead) {
			DataPacket packet;
			try {
				packet = new DataPacket(receivePacket.getData(), receivePacket.getLength());
				data[packet.getIntBN()] = packet.getData();
			} catch (Exception e) {
				endClient("Something Unexpected happened");
			}

		} else {
			try {
				ACKPacket packet = new ACKPacket(receivePacket.getData(), receivePacket.getLength());
			} catch (Exception e) {
				endClient("Something Unexpected happened");
			}
		}

	}

	private byte[][] getFile() {
		byte[][] data = new byte[1024][];
		try {
			FILEUtil file = new FILEUtil(PATH + requestPacket.getFilename());
			data = file.getData();
		} catch (FileNotFoundException e) {
			String error = e.getMessage();
			checkIOErrors(error);
		}

		return data;

	}

	private void saveData(byte[][] data) {
		try {
			FILEUtil file = new FILEUtil(data, PATH + requestPacket.getFilename(), true);
		} catch (Exception e) {
			String error = e.getMessage();
			checkIOErrors(error);

		}
	}

	private Request createRequest(String type) {
		if (type == null || type.isEmpty()) {
			endClient("Ending client because the type of request was empty");
		}

		try {
			return new Request(type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error, wrong type of request entered by user, client will end now");
			endClient("ending client because of wrong type of request");
		}
		return null;
	}

	private RequestPacket showInterface() {

		System.out.println("Introduce the type of operation(type 1 for read, type 2 for write)");
		String value = scanner.nextLine();
		String temp = "";
		if (value.equals("1"))
			temp = "read";
		if (value.equals("2"))
			temp = "write";

		Request request = createRequest(temp);

		System.out.println("Enter filename (including its extension): ");
		String filename = scanner.nextLine();

		System.out.println("Enter the mode");
		String mode = scanner.nextLine();
		
		return new RequestPacket(request, filename, mode);
	}

	private void endClient(String msj) {
		System.out.println(msj);
		sendReceiveSocket.close();
		scanner.close();

		System.exit(1);
	}

	// check for error 5
	// this error only occurs when during the transfer the port IDs are different
	// than the one in the initial connection;
	private void checkForIDError() {

		if (receivePacket.getPort() != originalPort) {
			System.out.println("Got an unknown tranfer ID  (Error 5)");

			ErrorPacket error = new ErrorPacket(5, "Unknown transfer ID");
			error.setDatagramPacket(receivePacket.getAddress(), receivePacket.getPort());

			try {
				sendReceiveSocket.send(error.getDatagramPacket());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Just responded to the Unknown transfer ID with Error Code 5");
			}
		}
	}

	private void checkIOErrors(String error) {

		if (error.equals("NotFound")) {
			System.out.println("File Not Found");
			endClient("Error 1 (File not found)");
		}

		else if (error.equals("WErr")) {
			System.out.println("Access denied");
			endClient("Ending client Error 2 (Access denied");
		}

		else if (error.equals("SErr")) {
			System.out.println("Disk is full, cannot save the file");
			endClient("Ending client Error 3 (Disk is full)");

		}

		if (error.equals("OWErr")) {
			System.out.println("File already exists and cannot overrite");
			endClient("Ending client Error 6 (File already exists");
		} else {
			System.out.print("Something unexpected happended");
			endClient("Ending client Error 0 (Not defined) " + error);
		}

	}

	private void sendPacket(Packet dp) {
		dp.setDatagramPacket(serverAddress, serverPort);
		sendPacket = dp.getDatagramPacket();
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean receivePacket() {
		try {
			receivePacket = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);
			//sendReceiveSocket.setSoTimeout(500);
			sendReceiveSocket.receive(receivePacket);

			if (originalPort != null)
				checkForIDError();
			checkForCorruptionError();

			BlockNum btemp = new BlockNum(Arrays.copyOfRange(receivePacket.getData(), 2, 4));

			itNum = 0;

			if (blockExpected == btemp.getInt()) {
				received = true;
				if (!packetsReceived.contains(btemp.getInt())) {
					packetsReceived.add(btemp.getInt());
					blockToSend = btemp.getInt() + 1;
					blockReceived =  btemp.getInt();
					blockExpected++;
				}
			} else {
				received = false;
			}
			
			boolean endWrite = !isRead && btemp.getInt() != 0 && received && sendPacket.getLength() < MAX_CAPACITY;
			boolean endRead = isRead &&  btemp.getInt() != 0 && received && sendPacket.getLength() < MAX_CAPACITY;

			return (isRead) ? endRead : endWrite;

		} catch (SocketTimeoutException e1) {
			String temp = (isRead) ? "DataPacket" : "ACKPacket";
			System.out.println(temp + " receive timed-out... retrying");
			itNum++;
			received = false;
			if (itNum > 50)
				endClient(temp + " Timeout could not get packet #" + blockReceived);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	

	/**
	 * Main method of Client class
	 */
	public static void main(String args[]) {
		Clientaso client = new Clientaso();
		client.sendReceive();
	}
}
