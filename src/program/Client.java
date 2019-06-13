

package program;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JOptionPane;
import utilities.*;
import utilities.client.*;
import utilities.packets.*;

public class Client {
	
	protected static final String PATH = "C:\\Jose\\Java\\files\\";
	protected static final boolean VERBOSE = true;	
	protected static final int MAX_CAPACITY = 512;
	protected static final int SERVER_PORT = 69;
	protected static final int SIMULATOR_PORT = 29;

	protected int serverPort;
	//used to check for error 5, this is set as soon as the server responds and remains constant
	protected Integer originalPort;
	protected InetAddress serverAddress;
	protected DatagramSocket sendReceiveSocket;	
	protected boolean transmissionEnd;
	
	private String filename;
	private String mode;
	private String hostName;
	private Request request;
	private Scanner scanner = new Scanner(System.in);
	private int port;

	// constructor
	public Client() {		
		
	}

	public void sendReceive() {
		String shutdown = null;
		
		System.out.println("CLIENT");
		setServerAddress();
		System.out.println("Type 1 for Normal mode or type anything else for Simulator mode ");
		String m = scanner.nextLine();
		port = (m.equals("1")) ? SERVER_PORT : SIMULATOR_PORT;
		
		do {	
		showInterface();	
		
		reset();

		RequestPacket RPacket = new RequestPacket(request, filename, mode);

		if (request.getType().equalsIgnoreCase("read")) {
			new ClientRR(RPacket, serverAddress, serverPort);
		} else if (request.getType().equalsIgnoreCase("write")) {
			new ClientWR(RPacket, serverAddress, serverPort);
		} else
			System.out.println("Could not contact server");
	
		System.out.println("\n");
		System.out.println("Transfer ended.");
		System.out.println("\nType 1 to continue with another tranfer, type  0 to end");
		
		shutdown = scanner.nextLine();
		
		}
		while(shutdown == null || shutdown.contentEquals("1"));
		if(shutdown.equals("0")) {
			System.out.println("Ending client by command");
		}
		else System.out.println("Ending client wrong command");
		System.exit(1);
		scanner.close();
	}

	public void reset() {
		if(sendReceiveSocket != null) sendReceiveSocket.close();
		transmissionEnd = false;
		
		originalPort = -1;
		serverPort = port;		
		
	}
	
	public Request createRequest(String type) {
		if(type == null || type.isEmpty()) {
			endClientTransfer("Ending client because the type of request was empty");
		}
				
		try {
			return new Request(type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error, wrong type of request entered by user, client will end now");
			endClientTransfer("ending client because of wrong type of request");
		}
		return null;
	}

	private void showInterface() {
		
		System.out.println("Introduce the type of operation(type 1 for read, type 2 for write)");
		String value = scanner.nextLine();
		String temp = "";
		if(value.equals("1")) temp = "read";
		if(value.equals("2")) temp = "write";
		
		request = createRequest(temp);
		
		System.out.println("Enter filename (including its extension): ");
		filename = scanner.nextLine();
		
		System.out.println("Enter the mode");
		mode = scanner.nextLine();
		
		System.out.println("MAKE SURE SERVER IS RUNNING AND LISTENING ON PORT 69");
		System.out.println("IF WANT TO USE SIMULATOR MAKE SURE IT IS RUNNING AND LISTENING ON PORT 29");
		System.out.println("\nPRESS ENTER WHEN YOU ARE READY");
		scanner.nextLine();
	}

	public void endClientTransfer(String msj) {
		System.out.println(msj);
		sendReceiveSocket.close();
		transmissionEnd = true;
	}
	
	//check for error 5
	//this error only occurs when during the transfer the port IDs are different than the one in the initial connection;
	public void checkForIDError(DatagramPacket dp) {
				
		if(dp.getPort() != originalPort) {
			System.out.println("Got an unknown tranfer ID  (Error 5)");
			
			ErrorPacket error = new ErrorPacket(5, "Unknown transfer ID");
			error.setDatagramPacket(dp.getAddress(), dp.getPort());
			
			try {
				sendReceiveSocket.send(error.getDatagramPacket());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Just responded to the Unknown transfer ID with Error Code 5");
			}
		}
	}
	
	
	//Check for IOERRORS (ONLY CATCHES EXCEPTIONS)
	//ERROR 4 DEALS WITH ACTUAL IO Errors
	public void checkIOErrors(String error) {		
		
		if(error.equals("NotFound")) {
			System.out.println("File Not Found");
			endClientTransfer("Error 1 (File not found)");
		}
		
		else if(error.equals("WErr")) {
			System.out.println("Access denied");
			endClientTransfer("Ending client Error 2 (Access denied");
		}
		
		else if(error.equals("SErr")) {
			System.out.println("Disk is full, cannot save the file");
			endClientTransfer("Ending client Error 3 (Disk is full)");
			
		}
		
		else if(error.equals("OWErr")) {
			System.out.println("File already exists and cannot overrite");
			endClientTransfer("Ending client Error 6 (File already exists");
		}
		else {
			System.out.print("Something unexpected happended");
			endClientTransfer("Ending client Error 0 (Not defined) " + error);
		}
		
		transmissionEnd = true;
		
	}
	
	public void setServerAddress() {
		System.out.println("Enter the server address or type 1 to set to localhost");
		
		hostName = scanner.nextLine();
		
		
		try {
			serverAddress = (hostName.equals("1")) ? InetAddress.getLocalHost():  InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	public void test() {
		String[] files = {"203p.jpg"};
		
		System.out.println("CLIENT");
		setServerAddress();
		System.out.println("Type 1 for Normal mode or type anything else for Simulator mode ");
		port = (scanner.nextLine().equals("1")) ? SERVER_PORT : SIMULATOR_PORT;
		
		String tempRequest;
		
		for(int f = 	0; f < files.length; f++) {
			
			for(int i = 0; i < 28; i++) {		//28 non I/O possibles cases
				if(serverAddress == null) setServerAddress();				
				
				if(i < 14) { //14 are on read / 14 are on write
					tempRequest = "READ"; //readRequest			
				}
				else {
					tempRequest = "WRITE"; //write				
				}
				
				filename = files[f];
				mode = "octet";
				
				System.out.println("\nPARAMETERS ARE SET TO DO: ");
				System.out.println(tempRequest + " Request ");
				System.out.println(filename + " filename ");
				System.out.println(mode + " mode ");
				
				System.out.println("Press a enter to continue (MAKE SURE SIMULATOR IS READY)\n");
				scanner.nextLine();
				
				request = createRequest(tempRequest);
				
				reset();
				
				RequestPacket RPacket = new RequestPacket(request , filename, mode);
		
				if (request.getType().equalsIgnoreCase("read")) {
					new ClientRR(RPacket, serverAddress, port);
				} else if (request.getType().equalsIgnoreCase("write")) {
					new ClientWR(RPacket, serverAddress, port);
				} else
					System.out.println("Could not contact server");
				
				
			}
		}
		System.exit(1);
		scanner.close();
		
	}
	/**
	 * Main method of Client class
	 */
	public static void main(String args[]) {
		Client client = new Client();
		client.sendReceive();
		
		//client.test();
	}

}