package program;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;
 
import utilities.FILEUtil;
import utilities.Request;
import utilities.TFTPUtil;
import utilities.packets.*;

public class Client {

	private static final int SERVER_PORT = 60300;
	private static final String SERVER_ADDRESS = "174.114.91.59";
	// private static final String SIMULATOR_ADDRESS = "";
	// private static final int SIMULATOR_PORT = 0;
	private static final String PATH = "C:\\Users\\AyeJay\\Desktop\\files\\";

	// datagram socket used to send/receive packets
	private DatagramSocket sendReceiveSocket;

	// constructor
	public Client() {
		sendReceiveSocket = TFTPUtil.datagramSocket();
	}

	public void sendReceive() {
		// creates a request Object with the type of request entered by the user
		Request request = createRequest("write");// createRequest(JOptionPane.showInputDialog("File transfer operation
													// (Write or Read):"));
		// saves the filename that the user enters
		String filename = "CU.jpg";// JOptionPane.showInputDialog("Enter the file name :");
		// saves the mode that the user enters.
		String mode = "octet";// JOptionPane.showInputDialog("Enter the mode :").toLowerCase();
		// creates a requestPacket
		RequestPacket RPacket = new RequestPacket(request, filename, mode);

		if (request.getType().equalsIgnoreCase("read")) {
			// readFile(RPacket);
		} else if (request.getType().equalsIgnoreCase("write")) {

			int port = establishWRQ(RPacket);

			if (port != -1) {
				writeFile(RPacket, port);
			} else {
				System.out.println("something went wrong, could not connect to server");
			}
		} else
			System.out.println("Could not contact server");

		sendReceiveSocket.close(); // closes the socket
	}

	public int establishWRQ(RequestPacket rp) {
		// will indicate if connection is successful;
		int port = -1;

		// prepares a datagramPacket to be sent
		try {
			//rp.setDatagramPacket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
			rp.setDatagramPacket(InetAddress.getLocalHost(), 69);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		// sends the datagram
		TFTPUtil.send(sendReceiveSocket, rp.getDatagramPacket(), "Trying to connect to server...");

		// prepare for the response
		DatagramPacket response = TFTPUtil.datagramPacket(4);

		// receive ack...
		TFTPUtil.receive(sendReceiveSocket, response, "Waiting for ACK...");

		// extract info
		ACKPacket ack = new ACKPacket(response.getData());

		// check if ACK is valid
		byte[] ackBytes = ack.getID();
		int ackBN = ack.getIntBN();

		if (ackBytes[0] == 0 && ackBytes[1] == 4 && ackBN == 0)// byte[1] must be 4 not 1
			port = response.getPort();

		return port;
	}

	public void writeFile(RequestPacket rp, int port) {
		System.out.println("WriteFile Method...");

		String path = PATH + rp.getFilename();
		FILEUtil file = new FILEUtil(path);

		byte[][] data = file.getData();
		DataPacket[] dataPackets = new DataPacket[data.length];

		System.out.println("creating DataPackets...");
		for (int i = 0; i < data.length; i++) {
			dataPackets[i] = new DataPacket(i + 1, data[i]);

			//System.out.println("DATA LENGTH from packet# " + (i + 1) + " Length: " + data[i].length);
			//System.out.println("PACKET LENGTH " + dataPackets[i].getPacket().length);
		}

		System.out.println("Sending DataPackets...");
		for (DataPacket dp : dataPackets) {
			try {
				//dp.setDatagramPacket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
				dp.setDatagramPacket(InetAddress.getLocalHost(), port);
			} catch (UnknownHostException e) { // TODO Auto-generated catch block
				e.printStackTrace();
			}

			TFTPUtil.send(sendReceiveSocket, dp.getDatagramPacket(), "Sending Packet #" + dp.getIntBN());

			DatagramPacket ack = TFTPUtil.datagramPacket(4);

			TFTPUtil.receive(sendReceiveSocket, ack, "Waiting for ACK...");

			ACKPacket ackPacket = new ACKPacket(ack.getData());

			System.out.println("Got ACK PACKET #" + ackPacket.getIntBN());

		}

		System.out.println("FINISH WRITING...");

	}

	public void transfer(RequestPacket rp) {

	}

	public Request createRequest(String type) {
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

	/**
	 * Main method of Client class
	 */
	public static void main(String args[]) {
		Client client = new Client();
		client.sendReceive();
	}
}