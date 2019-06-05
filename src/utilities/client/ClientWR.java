package utilities.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import program.*;
import utilities.FILEUtil;
import utilities.packets.*;

public class ClientWR extends Client {

	private RequestPacket requestPacket;
	private FILEUtil file;
	private byte[][] data;
	private DataPacket dataPacket = null;
	
	public ClientWR(RequestPacket requestPacket) {

		this.requestPacket = requestPacket;

		try {
			file = new FILEUtil(PATH + requestPacket.getFilename());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		data = file.getData();
		transfer();
	}

	private void transfer() {
		//sending request...
		this.requestPacket.setDatagramPacket(serverAddress, serverPort);

		try {
			sendReceiveSocket.send(requestPacket.getDatagramPacket());
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		DatagramPacket dp = new DatagramPacket(new byte[512], 512);
		
		try {
			sendReceiveSocket.receive(dp);
			serverPort = dp.getPort();
			serverAddress = dp.getAddress();
	
			ACKPacket temp = null;
			try {
				temp = new ACKPacket(dp.getData(), dp.getLength());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("GOT FIRST PACKET (REQUEST)  PACKET#" + temp.getIntBN());
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		
		
		//writing file...
		System.out.println("Writing File...");
		int i = 1;
		int tNum = 0;

		//this loop will first get response from server then if no time out, will send a packet
		while (i < data.length + 1) {

			dataPacket = new DataPacket(i , data[i - 1]);
			dataPacket.setDatagramPacket(serverAddress, serverPort);
			System.out.println("");
			
			try {
				sendReceiveSocket.send(dataPacket.getDatagramPacket());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			
			DatagramPacket response = new DatagramPacket(new byte[4], 4);

			try {
				sendReceiveSocket.setSoTimeout(500);
				sendReceiveSocket.receive(response); 
				
				ACKPacket ackPacket = null;
				try {
					ackPacket = new ACKPacket(response.getData(), response.getLength());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("got it....");
				System.out.println(i + " vs " +ackPacket.getIntBN());
				
				
				if (i == ackPacket.getIntBN() ) {
					i++;
				}				
				
				tNum = 0;
			} catch (SocketTimeoutException e1) {
				System.out.println("ACK receive timed-out... retrying");
				tNum++;
				if(tNum > 50) {
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}