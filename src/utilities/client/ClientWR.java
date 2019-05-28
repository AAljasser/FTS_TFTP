package utilities.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import program.*;
import utilities.FILEUtil;
import utilities.TFTPUtil;
import utilities.packets.*;

public class ClientWR extends Client {

	private RequestPacket requestPacket;
	private FILEUtil file;
	private byte[][] data;
	private int responsePort;
	private InetAddress responseAddress;

	public ClientWR(RequestPacket requestPacket) {

		this.requestPacket = requestPacket;

		try {
			file = new FILEUtil(PATH + requestPacket.getFilename());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		data = file.getData();
		responsePort = -1;

		transfer();
	}

	private void transfer() {
		//sending request...
		this.requestPacket.setDatagramPacket(serverAddress, serverPort);

		TFTPUtil.send(sendReceiveSocket, this.requestPacket.getDatagramPacket(), "Trying to connect to server...");

		//writing file...
		System.out.println("Writing File...");
		int i = 0;
		int tNum = 0;

		//this loop will first get response from server then if no time out, will send a packet
		while (i < data.length) {

			DatagramPacket response = TFTPUtil.datagramPacket(4);

			try {
				sendReceiveSocket.setSoTimeout(500);
				sendReceiveSocket.receive(response); 
				ACKPacket ackPacket = new ACKPacket(response.getData(), response.getLength());

				if(ackPacket.getIntBN() == 0) {
					responseAddress = response.getAddress();
					responsePort = response.getPort();
				}
				if (i == ackPacket.getIntBN()) {
					i++;
				}				
				
				DataPacket dp = new DataPacket(i , data[i -1]);

				dp.setDatagramPacket(responseAddress, responsePort);

				TFTPUtil.send(sendReceiveSocket, dp.getDatagramPacket(), "Sending Packet #" + dp.getIntBN());
				
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
