package utilities.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		serverPort = SERVER_PORT;

		try {
			file = new FILEUtil(PATH + requestPacket.getFilename());
		} catch (FileNotFoundException e) {
			String error = e.getMessage();			
			checkIOErrors(error);			
		}

		data = file.getData();
		transfer();
	}
	
	private void transfer() {
		int tNum = 0;
		int i = 1;
		boolean gotACK0 = false;

		// try to get the first ackPakcet....
		while (!gotACK0 && !transmissionEnd) { 
			// sending request...
			this.requestPacket.setDatagramPacket(serverAddress, serverPort);

			try {
				sendReceiveSocket.send(requestPacket.getDatagramPacket());
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

			DatagramPacket dp = new DatagramPacket(new byte[512], 512);

			try {
				sendReceiveSocket.setSoTimeout(500);
				sendReceiveSocket.receive(dp);

				serverPort = dp.getPort();
				originalPort = serverPort; // used to check for ID errors
				serverAddress = dp.getAddress();

				// if we get a corrupted packet we create an error packet, send it, and
				// terminate.
				// if we get an error packet we terminate
				// else we return the ackPacket
				ACKPacket temp = checkForCorruptionError(dp, 0);
				if(transmissionEnd) break;
				if (VERBOSE)
					System.out.println("GOT FIRST PACKET (REQUEST)  PACKET#" + temp.getIntBN());
				gotACK0 = true;
			} catch (SocketTimeoutException e1) {
				System.out.println("DataPacket receive timed-out... retrying");
				tNum++;
				if (tNum > 50) {
					System.out.println("Could not get ACK #0");
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		//writing file...
		System.out.println("Writing File...");		
		tNum = 0;
		//this loop will first get response from server then if no time out, will send a packet
		while ((i < data.length + 1) && !transmissionEnd) {

			dataPacket = new DataPacket(i , data[i - 1]);
			dataPacket.setDatagramPacket(serverAddress, serverPort);
			System.out.println("");
			
			//send DATA (no need to check for error here)
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
				
				checkForIDError(response);
				ACKPacket ackPacket = checkForCorruptionError(response, i);
				if(transmissionEnd) break;
				if(VERBOSE) System.out.println("sent DataPacket #" + i + " got ACK #" + ackPacket.getIntBN());
				
				if (i == ackPacket.getIntBN()) i++;					
				
				tNum = 0;
				
			} catch (SocketTimeoutException e1) {
				System.out.println("ACK receive timed-out... retrying");
				tNum++;
				if(tNum > 50) {
					System.out.println("Could not get ACK #" + i);
					break;
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		 System.out.println("Finished Writing");
	}
	
	//check for error 4
	//in clients:
	//this error  occurs when the opCode of the datagramPacket receive does not coincide with the opCode of the expected packet.
	//this error also occurs when block num is invalid. 
	private ACKPacket checkForCorruptionError(DatagramPacket dp, int expectedPacket) {
		ACKPacket temp = null;
		try {
			temp = new ACKPacket(dp.getData(), dp.getLength());
			
		} catch (Exception e) {
			
			if(e.getMessage().equals("OPCODE")) {
				ErrorPacket err = new ErrorPacket(4, "illegal TFTP operation");
				err.setDatagramPacket(serverAddress, serverPort);
				
				try {
					sendReceiveSocket.send(err.getDatagramPacket());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//e.printStackTrace();
				endClientTransfer("Ending client  Error 4 on OPCODE (Illegal TFTP operation)");
				return null;
			}
			else if(e.getMessage().equals("BNUMBER")) {
				ErrorPacket err = new ErrorPacket(4, "illegal TFTP operation");
				err.setDatagramPacket(serverAddress, serverPort);
				
				try {
					sendReceiveSocket.send(err.getDatagramPacket());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				endClientTransfer("Ending client  Error 4 on Block Number (Illegal TFTP operation)");
				return null;
			}
		}
		
		if(temp.isError()) {
			System.out.println("Error Code:"+ temp.getErrorPacket().getIntBN() + " " + temp.getErrorPacket().getMsg());
			
			String msg = (temp.getErrorPacket().getMsg().isEmpty()) ? "" : "(" + temp.getErrorPacket().getMsg() +" )";
			endClientTransfer("Ending client ERROR 4 " +" " + msg);
			return null;
		}
		
			
		
		return temp;
	}
}