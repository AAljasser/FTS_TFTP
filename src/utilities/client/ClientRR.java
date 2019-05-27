package utilities.client;

import utilities.FILEUtil;
import utilities.TFTPUtil;
import utilities.packets.*;

import java.net.DatagramPacket;
import java.util.Arrays;

import program.*;

public class ClientRR extends Client {

	RequestPacket requestPacket;

	public ClientRR(RequestPacket requestPacket) {
		this.requestPacket = requestPacket;
		initiate();
	}

	public void initiate() {
		establishRequest();
		transfer();

	}

	private void establishRequest() {
		this.requestPacket.setDatagramPacket(getServerAddress(), getServerPort());

		TFTPUtil.send(getSocket(), this.requestPacket.getDatagramPacket(), "Trying to connect to server...");
	}

	private void transfer() {
		boolean is512 = true;
		byte[][] data = new byte[1024][];
		int blockNumber = 0;

		while (is512) {
			DatagramPacket dgp = TFTPUtil.datagramPacket(MAX_CAPACITY);

			TFTPUtil.receive(getSocket(), dgp, "Waiting for DataACK...");

			DataPacket response = new DataPacket(dgp.getData(), dgp.getLength());			
			
			if(blockNumber == response.getIntBN()) {
				
				data[blockNumber++] = Arrays.copyOfRange(dgp.getData(), 4, dgp.getLength());	
	
				ACKPacket ack = new ACKPacket(response.getIntBN());
	
				ack.setDatagramPacket(getServerAddress(), dgp.getPort());
	
				TFTPUtil.send(getSocket(), ack.getDatagramPacket(), "Sending ACK # " + response.getIntBN());
	
				if (dgp.getLength() < 512) {
					is512 = false;
					System.out.println("FINISHED Reading...");
				}
			}
		}
		data = Arrays.copyOfRange(data, 0, blockNumber);
		
		FILEUtil file = new FILEUtil(data);

		file.saveFile(PATH + this.requestPacket.getFilename());

	}

}
