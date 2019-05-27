package utilities.client;

import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.InetAddress;

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

		initiate();
	}

	public void initiate() {
		establishRequest();
		transfer();
	}

	private void establishRequest() {

		this.requestPacket.setDatagramPacket(getServerAddress(), getServerPort());

		TFTPUtil.send(getSocket(), this.requestPacket.getDatagramPacket(), "Trying to connect to server...");

		DatagramPacket response = TFTPUtil.datagramPacket(4);

		TFTPUtil.receive(getSocket(), response, "Waiting for ACK...");

		ACKPacket ack = new ACKPacket(response.getData(), response.getLength());

		byte[] ackBytes = ack.getID();
		int ackBN = ack.getIntBN();

		if (ackBytes[0] == 0 && ackBytes[1] == 4 && ackBN == 0) {
			responsePort = response.getPort();
			responseAddress = response.getAddress();
		}
	}

	private void transfer() {
		System.out.println("Writing File...");
		int i = 0;

		while(i < data.length-1) {
			DataPacket dp = new DataPacket(i + 1, data[i]);

			dp.setDatagramPacket(responseAddress, responsePort);
			TFTPUtil.send(getSocket(), dp.getDatagramPacket(), "Sending Packet #" + dp.getIntBN());
			
			DatagramPacket ack = TFTPUtil.datagramPacket(4);
			
			TFTPUtil.receive(getSocket(), ack, "Waiting for ACK...");
			
			ACKPacket ackPacket = new ACKPacket(ack.getData(), ack.getLength());
			
			//System.out.println(ackPacket.getIntBN());

			if(i == ackPacket.getIntBN()-1) {
				i++;
			}

		}
		DataPacket dp = new DataPacket(i + 1, data[i]);

		dp.setDatagramPacket(responseAddress, responsePort);
		TFTPUtil.send(getSocket(), dp.getDatagramPacket(), "Sending Packet #" + dp.getIntBN());

		System.out.println("FINISH WRITING...");

	}

}
