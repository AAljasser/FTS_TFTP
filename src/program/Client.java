

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
	
	protected static final boolean VERBOSE = true;
	protected static final String PATH = "C:\\Jose\\Java\\files\\";
	protected static final int MAX_CAPACITY = 512;
	protected static final int SERVER_PORT = 69;

	protected int serverPort;
	//used to check for error 5, this is set as soon as the server responds and remains constant
	protected Integer originalPort;
	protected InetAddress serverAddress;
	protected DatagramSocket sendReceiveSocket;	
	protected boolean transmissionEnd;
	
	private String filename;
	private String mode;
	private Request request;
	private Scanner scanner = new Scanner(System.in);

	// constructor
	public Client() {		
		try {
			serverAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void sendReceive() {
		String shutdown = null;
		
		do {
		reset();
		
		showInterface();	
		

		RequestPacket RPacket = new RequestPacket(request, filename, mode);

		if (request.getType().equalsIgnoreCase("read")) {
			new ClientRR(RPacket);
		} else if (request.getType().equalsIgnoreCase("write")) {
			new ClientWR(RPacket);
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
		request = null;
		filename = null;
		mode = null;
		originalPort = -1;
		serverPort = SERVER_PORT;
		try {
			serverAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		System.out.println("Introduce the type of operation(type 1 for read, type 2 for write");
		String value = scanner.nextLine();
		String temp = "";
		if(value.equals("1")) temp = "read";
		if(value.equals("2")) temp = "write";
		
		request = createRequest(temp);
		
		System.out.println("Enter filename (including its extension): ");
		filename = scanner.nextLine();
		
		System.out.println("Enter the mode");
		mode = scanner.nextLine();		
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
		
		if(error.equals("OWErr")) {
			System.out.println("File already exists and cannot overrite");
			endClientTransfer("Ending client Error 6 (File already exists");
		}
		else {
			System.out.print("Something unexpected happended");
			endClientTransfer("Ending client Error 0 (Not defined) " + error);
		}
		
	}
	/**
	 * Main method of Client class
	 */
	public static void main(String args[]) {
		Client client = new Client();
		client.sendReceive();
	}

}