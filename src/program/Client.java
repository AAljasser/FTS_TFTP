
//SIMULATOR V2.0

package program;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import utilities.*;
import utilities.client.*;
import utilities.packets.*;

public class Client {
	
	//protected static final boolean VERBOSE = false;
	protected static final String PATH = "C:\\Users\\AyeJay\\Desktop\\files\\";
	protected static final int MAX_CAPACITY = 512;

	protected int serverPort;
	protected InetAddress serverAddress;
	protected DatagramSocket sendReceiveSocket;
	
	private String filename;
	private String mode;
	private Request request;

	// constructor
	public Client() {
		sendReceiveSocket = TFTPUtil.datagramSocket();
		serverPort = 69;
		try {
			serverAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void sendReceive() {

		showInterface();

		RequestPacket RPacket = new RequestPacket(request, filename, mode);

		if (request.getType().equalsIgnoreCase("read")) {
			new ClientRR(RPacket);
		} else if (request.getType().equalsIgnoreCase("write")) {
			new ClientWR(RPacket);
		} else
			System.out.println("Could not contact server");

		sendReceiveSocket.close(); // closes the socket
	}

	
	public Request createRequest(String type) {
		if(type == null) {
			endClient();
		}
				
		try {
			return new Request(type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Wrong type of request, try typing 'write' or 'read'", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	private void showInterface() {
		// creates a request Object with the type of request entered by the user
		request = createRequest(JOptionPane.showInputDialog(null, "File transfer operation(Write or Read):", "Client Interface", JOptionPane.DEFAULT_OPTION));
		// saves the filename that the user enters
		filename = JOptionPane.showInputDialog(null, "Enter filename: ", "Client Interface", JOptionPane.DEFAULT_OPTION);
		if(filename == null) endClient();
		// saves the mode that the user enters.
		mode = JOptionPane.showInputDialog(null, "Enter mode: ", "Client Interface", JOptionPane.DEFAULT_OPTION).toLowerCase();
		if(mode == null) endClient();
		
	}

	public void endClient() {
		JOptionPane.showMessageDialog(null, "Error, client will close now", "ERROR",JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
	/**
	 * Main method of Client class
	 */
	public static void main(String args[]) {
		Client client = new Client();
		client.sendReceive();
	}

}