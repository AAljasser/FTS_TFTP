//Simulator V2.0 based on simulator V1.0 and simulator from assignment 1;
package program;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import utilities.ArrayUtil;
import utilities.packets.*;
import utilities.simulator.Parameters;

public class Simulator2 {
	
	private static final int MAX_CAPACITY = 512;
	private static final int SIMULATOR_PORT = 29;

	
	// UDP datagram packets and sockets used to send / receive
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket clientSocket, serverSocket;
		
	//interface to get the parameters i.e. delay, duplicate, number of packets, etc
	private Parameters parameters;
	
	//stuff to keep track of requestType (to end this program)
	private RequestPacket requestPacket;
	private byte[] requestType;
	private boolean isRead = false;
	private int clientPort = -1;
	private int serverPort = -1;
	
	//array to keep track of packets that have been processed;
	private ArrayList<Integer> packetsProcessed = new ArrayList<Integer>();

	// constructor
	public Simulator2() {
		parameters = new Parameters();

		try {
			clientSocket = new DatagramSocket(SIMULATOR_PORT);
			serverSocket = new DatagramSocket();			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	// ''main method'' in this method user sets some fields on a simple user
	// interface then we go to the main sequence.
	private void listen() {
		// shows interface
		showInterface();

		getRequest();
		
		if(isRead) {
			readSequence();
		}
		
		else {
			writeSequence();
		}
		
	}
	
	private void getRequest() {
		
		System.out.println("starting simulator...");
		
		try {
			
			receivePacket = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);
			clientSocket.receive(receivePacket);
			
			try {
				requestPacket = new RequestPacket(receivePacket.getData(), receivePacket.getLength());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			requestType = requestPacket.getID();
			
			clientPort = receivePacket.getPort();
			
			System.out.println("got request...");
			
			if(requestType[1] == 1) {
				isRead = true;
			}
			else {
				isRead = false;
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void writeSequence() {
		
		boolean is512 = true;
		boolean packetLost = false;
		
		try {
			 sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), 69);
			serverSocket.send(sendPacket); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("sent request...");
		
		while(is512) {			
			
			if(!packetLost) {
				
				System.out.println("packet is safe...");
			
				try {
					receivePacket = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);
					serverSocket.receive(receivePacket);
					serverPort = receivePacket.getPort();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
					clientSocket.send(sendPacket);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
						
			
			try {
				receivePacket = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);
				clientSocket.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			packetLost = sendToServerOnWrite();
			
			if(sendPacket.getLength() < 512) {
				System.out.println("ending simulator...");
				is512 = false;
			}
			
			System.out.println("packet sent...");
			
		}
		
	}

	
	public boolean sendToServerOnWrite() {
		
		boolean aux = false;
		
		DataPacket temp=null;
		try {
			temp = new DataPacket(receivePacket.getData(), receivePacket.getLength());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int packetNumber = temp.getIntBN();
		
		if(!packetsProcessed.contains(packetNumber) && packetNumber >= parameters.getFrom() && packetNumber <= parameters.getTo()) {
			manipulatePacket(receivePacket, serverSocket, packetNumber);
			
			aux = true;
			
		}
	
		
		
		try {
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);			
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aux;
	}
	
	
	private void readSequence() {

		boolean is512 = true;
		boolean packetLost = false;

		try {
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),	InetAddress.getLocalHost(), 69);
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("sent request...");

		while (is512) {

			try {
				receivePacket = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);
				serverSocket.receive(receivePacket);
				serverPort = receivePacket.getPort();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			packetLost = sendToClientOnRead();
			

			if (!packetLost) {

				System.out.println("packet is safe...");

				try {
					receivePacket = new DatagramPacket(new byte[MAX_CAPACITY], MAX_CAPACITY);
					clientSocket.receive(receivePacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
					serverSocket.send(sendPacket);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				

				System.out.println("packet sent...");

			}
		}
	}
	
	
	//method that does the operation selected on the interface (delay, duplicate, lost).
	//once a packet is processed (delayed, duplicated, etc),
	//that packet gets stored in an ArrayList to keep track of them and no repeat; 
	private Integer manipulatePacket(DatagramPacket packet, DatagramSocket socket,  int packetNumber) {
			int operationID = parameters.getOperationID();
			packetsProcessed.add(packetNumber);
			
			//delay packet
			if (operationID == 1) {
				try {
					Thread.sleep(1500); // TODO: this value should be defined on the GUI
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			//duplicate packet
			} else if (operationID == 2) {
				try {
					socket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			//lose packet
			} else if (operationID == 3) {
				try {
					Thread.sleep(1500); // TODO: this value should be defined on the GUI
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}			
		
		
		return operationID;
	}

	private boolean sendToClientOnRead() {
		boolean aux = false;
		
		DataPacket temp = null;
		try {
			temp = new DataPacket(receivePacket.getData(), receivePacket.getLength());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int packetNumber = temp.getIntBN();
		
		if(!packetsProcessed.contains(packetNumber) && packetNumber >= parameters.getFrom() && packetNumber <= parameters.getTo()) {
			manipulatePacket(receivePacket, clientSocket, packetNumber);
			
			aux = true;
			
		}
			
		try {
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);			
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aux;		
	}
	
	
	
	
	
	
	
	
	//method to show interface
	public void showInterface() {
		parameters.getInfo();
	}
	
	public static void main(String arg[]) {

		Simulator2 simulator = new Simulator2();
		simulator.listen();

	}

}