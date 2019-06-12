

package program;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import utilities.*;
import utilities.client.*;
import utilities.packets.*;

public class Client {
	
	protected static final boolean VERBOSE = true;
	protected static final String PATH = "C:\\Users\\josefrancojimenez\\Desktop\\files\\";
	protected static final int MAX_CAPACITY = 512;
	protected static final int SERVER_PORT = 69;
	
	private ParametersClient parameters;
	private boolean testMode = false;

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
			serverAddress = InetAddress.getByName("134.117.59.152");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void sendReceive() {
		System.out.println("CLIENT");
		System.out.println("type 1 to do a quick test, type something else to test a particular case");
		testMode = (scanner.nextLine().equals("1")) ?  true : false;
		if(testMode) test();
		
		else {
			String shutdown = null;
			
			do {
			reset();
			
			parameters.getInfo();	
			
			request = createRequest(parameters.getFilename());
	
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
			parameters.closeScanner();
		}
	}

	public void reset() {
		if(sendReceiveSocket != null) sendReceiveSocket.close();
		
		parameters = new ParametersClient();
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
	
	public void test() {
		String[] files = {"203p.jpg"};
		
		for(int f = 	0; f < files.length; f++) {
			
			for(int i = 14; i < 28; i++) {		//28 non I/O possibles cases
				reset();
				if(i < 14) { //14 are on read / 14 are on write
					parameters.setRequestType(1); //readRequest			
				}
				else {
					parameters.setRequestType(2); //write
					
				
				}
				
				parameters.setFilename(files[f]);
				parameters.setMode("octet");
				
				System.out.println("\nPARAMETERS ARE SET TO DO: ");
				System.out.println(parameters.getRequestName() + " Request ");
				System.out.println(parameters.getFilename() + " filename ");
				System.out.println(parameters.getMode()+ " mode ");
				
				System.out.println("Press a enter to continue (MAKE SURE SIMULATOR IS READY)\n");
				scanner.nextLine();
				
				request = createRequest(parameters.getRequestName());
				
				RequestPacket RPacket = new RequestPacket(request , parameters.getFilename(), parameters.getMode());
		
				if (request.getType().equalsIgnoreCase("read")) {
					new ClientRR(RPacket);
				} else if (request.getType().equalsIgnoreCase("write")) {
					new ClientWR(RPacket);
				} else
					System.out.println("Could not contact server");
				
				
			}
		}
		System.exit(1);
		scanner.close();
		parameters.closeScanner();
	}
	/**
	 * Main method of Client class
	 */
	public static void main(String args[]) {
		Client client = new Client();
		client.sendReceive();
	}

}