package program;

import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.swing.JOptionPane;
 
import utilities.FILEUtil;
import utilities.Request;
import utilities.TFTPUtil;
import utilities.*;
import utilities.packets.*;

public class Client {

	private static final String PATH = "C:\\Users\\AyeJay\\Desktop\\files\\";

	private static final int MAX_CAPACITY = 512;
	
	private   int SERVER_PORT ;
	private   InetAddress SERVER_ADDRESS;
	private InetAddress SIMULATOR_ADDRESS;
	private int SIMULATOR_PORT;

	// datagram socket used to send/receive packets
	private DatagramSocket sendReceiveSocket;

	// constructor
	public Client() {
		sendReceiveSocket = TFTPUtil.datagramSocket();
		SERVER_PORT = 69;
		SIMULATOR_PORT = 29;
	    try {
			SERVER_ADDRESS = InetAddress.getLocalHost();
			SIMULATOR_ADDRESS = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
	
	}

	public void sendReceive() {
		// creates a request Object with the type of request entered by the user
		Request request =createRequest(JOptionPane.showInputDialog("File transfer operation(Write or Read):"));
		// saves the filename that the user enters
		String filename = JOptionPane.showInputDialog("Enter the file name :");
		// saves the mode that the user enters.
		String mode =JOptionPane.showInputDialog("Enter the mode :").toLowerCase();
		// creates a requestPacket
		RequestPacket RPacket = new RequestPacket(request, filename, mode);

		if (request.getType().equalsIgnoreCase("read")) {
			establishRRQ(RPacket);
			readFile(RPacket);
			
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
		
		int port = -1;
		
		rp.setDatagramPacket(SERVER_ADDRESS, SERVER_PORT);		
		
		TFTPUtil.send(sendReceiveSocket, rp.getDatagramPacket(), "Trying to connect to server...");

		DatagramPacket response = TFTPUtil.datagramPacket(4);

		TFTPUtil.receive(sendReceiveSocket, response, "Waiting for ACK...");

		ACKPacket ack = new ACKPacket(response.getData());

		byte[] ackBytes = ack.getID();
		int ackBN = ack.getIntBN();

		if (ackBytes[0] == 0 && ackBytes[1] == 4 && ackBN == 0)// byte[1] must be 4 not 1
			port = response.getPort();

		return port;
	}

	public void writeFile(RequestPacket rp, int port) {
		System.out.println("WriteFile Method...");

		String path = PATH + rp.getFilename();
		FILEUtil file = null;
		try {
			file = new FILEUtil(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[][] data = file.getData();
		
		for (int i = 0; i < data.length; i++) {
			DataPacket dp = new DataPacket(i + 1, data[i]);

			dp.setDatagramPacket(SERVER_ADDRESS, port);
			
			TFTPUtil.send(sendReceiveSocket, dp.getDatagramPacket(), "Sending Packet #" + dp.getIntBN());

			if( i != data.length -1) {
				DatagramPacket ack = TFTPUtil.datagramPacket(4);
		
				TFTPUtil.receive(sendReceiveSocket, ack, "Waiting for ACK...");
		
				ACKPacket ackPacket = new ACKPacket(ack.getData());
		
				System.out.println("Got ACK PACKET #" + ackPacket.getIntBN());
			}

		}

		System.out.println("FINISH WRITING...");

	}

	public void establishRRQ(RequestPacket rp) {
		rp.setDatagramPacket(SERVER_ADDRESS, SERVER_PORT);
		
		TFTPUtil.send(sendReceiveSocket, rp.getDatagramPacket(), "Trying to connect to server...");
	}
	
	public void readFile(RequestPacket rp) {
		boolean is512 = true;
		byte[][] data = new byte[0][];
		//byte[][] data = new byte[512][508];
		//int counter =0;
		
		while(is512) {
			DatagramPacket dgp = TFTPUtil.datagramPacket(MAX_CAPACITY);
			
			TFTPUtil.receive(sendReceiveSocket, dgp, "Waiting for DataACK...");
			
			DataPacket dataPacket = new DataPacket(Arrays.copyOfRange(dgp.getData(),0,dgp.getLength()));
			
			data = ArrayUtil.pushBidimensional(data, dataPacket.getData());
			//data[counter] = Arrays.copyOfRange(dgp.getData(),4,dgp.getLength());
			//System.out.println(dgp.getLength());
			//counter++;
			
			ACKPacket ack = new ACKPacket(dataPacket.getIntBN());
			
			ack.setDatagramPacket(SERVER_ADDRESS, dgp.getPort());
			
			TFTPUtil.send(sendReceiveSocket, ack.getDatagramPacket(), "Sending ACK # " + dataPacket.getIntBN());
			
			if(dgp.getLength() < 512) {
				is512 = false;
				System.out.println("FINISHED Reading...");
			}
		}
		//data = Arrays.copyOfRange(data, 0, counter);
		FILEUtil file = new FILEUtil(data);
		//System.out.println(rp.getFilename());
		file.saveFile(PATH + rp.getFilename());
		
	}
	
	public void sendPacket(Packet packet, int port) {
				
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