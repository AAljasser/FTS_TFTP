package utilities.client;

import utilities.FILEUtil;
import utilities.packets.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import program.*;

public class ClientRR extends Client {

	RequestPacket requestPacket;

	public ClientRR(RequestPacket requestPacket) {
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		serverPort = SERVER_PORT;
		this.requestPacket = requestPacket;
		transfer();
	}

	private void transfer() {
		this.requestPacket.setDatagramPacket(serverAddress, serverPort);

		//sends the request...
		try {
			sendReceiveSocket.send( this.requestPacket.getDatagramPacket());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		boolean is512 = true;
		byte[][] data = new byte[65535][];
		int blockNumber = 0;
		int tNum = 0;
		
		System.out.println("Start Reading...");
		while (is512 && !transmissionEnd) {

			DatagramPacket dgp = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);

			try {
				sendReceiveSocket.setSoTimeout(500);
				sendReceiveSocket.receive(dgp);
				
				if(originalPort == null) {
					serverPort = dgp.getPort();
					originalPort = serverPort;
					serverAddress = dgp.getAddress();					
				} else {
					checkForIDError(dgp);			
				}
				DataPacket response = checkForCorruptionError(dgp, blockNumber);
				if(transmissionEnd) break;				
				
				ACKPacket ack = new ACKPacket(response.getIntBN());
				ack.setDatagramPacket(serverAddress, serverPort); 				 
				
				if(VERBOSE) System.out.println("expecting DataPacket" + blockNumber + " got #" + response.getIntBN());
				
				sendReceiveSocket.send(ack.getDatagramPacket());

				if (response.getIntBN() == blockNumber) {
					
					if(VERBOSE) System.out.println("saving data...");
						
					data[blockNumber++] = Arrays.copyOfRange(dgp.getData(), 4, dgp.getLength());
					
					if (dgp.getLength() < 512) {
						is512 = false;						
					}
					
				}
				
				tNum = 0;
			} catch (SocketTimeoutException e1) {
				System.out.println("ACKPacket wait timed-out... retrying");
				tNum++;
				if(tNum > 50) {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		
		data = Arrays.copyOfRange(data, 0, blockNumber);

		try {
			FILEUtil file = new FILEUtil(data,PATH + this.requestPacket.getFilename(),true);
		} catch (Exception e) {
			String error = e.getMessage();			
			checkIOErrors(error);
			
		}
		System.out.println("FINISHED Reading...");
	}
		
	//check for error 4
	//in clients:
	//this error  occurs when the opCode of the datagramPacket receive does not coincide with the opCode of the expected packet.
	//this error also occurs when block num is invalid. 
	private DataPacket checkForCorruptionError(DatagramPacket dp, int expectedPacket) {
		DataPacket temp = null;
		boolean end = false;
		try {
			temp = new DataPacket(dp.getData(), dp.getLength());			
			
		} catch (Exception e) {
			if(e.getMessage().equals("OPCODE")) {
				ErrorPacket err = new ErrorPacket(4, "illegal TFTP operation on OPCODE");
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
				ErrorPacket err = new ErrorPacket(4, "illegal TFTP operation on Block Number");
				err.setDatagramPacket(serverAddress, serverPort);
				
				try {
					sendReceiveSocket.send(err.getDatagramPacket());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				endClientTransfer("Ending client  Error 4 on BLOCK NUMBER (Illegal TFTP operation)");
				return null;
			}
		}
			
		if(temp.isError()) {
			System.out.println("Error Code:"+ temp.getErrorPacket().getIntBN()+ " " +  temp.getErrorPacket().getMsg());
			
			String msg = (temp.getErrorPacket().getMsg().isEmpty()) ? "" : "(" + temp.getErrorPacket().getMsg() +" )";
			endClientTransfer("Ending client ERROR 4 " +" " + msg );
			return null;
		}
		
		
		return temp;
	}

}