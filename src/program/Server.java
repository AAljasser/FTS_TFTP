package program;
import java.io.*;
import java.net.*;
import java.util.Arrays;

import javax.swing.JOptionPane;

import java.util.*;
import java.lang.*;
import utilities.*;
import utilities.packets.*;


public class Server implements Runnable {
	private InetAddress ClientAddress = null;
	private int ClientPort;
	private DatagramPacket ClientPacket;
	private byte rawData[] = new byte[512];
	private int threadCounter = 0;
	private int rType = 0; // 0 - Server, 1 - WR, 2 - RR
	private String rFN;
	private String rM;
	private String dir = "C:\\Jose\\Java\\files\\save\\";
	//*** TEMP
	private DatagramSocket ServerSocket = null;
	Thread closure;
	Map<Integer, Thread> clientsT = new HashMap<Integer, Thread>();
	Thread clientT;
	
	
	public Server(int port) {
		try {
			this.ServerSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Server socker couldn't be created, exiting...");
			e.printStackTrace();
			System.exit(1);
		}
	}
	public Server(DatagramPacket CP, int t,byte[] rd) {
		RequestPacket temp = new RequestPacket(rd, rd.length);
		this.ClientPacket = CP;
		this.ClientAddress = CP.getAddress();
		this.ClientPort = CP.getPort();
		this.rType = t;
		this.rawData = rd;
		this.rFN = temp.getFilename();
		this.rM = temp.getMode();
	}
	public void Start() {
		Thread clientPro = null;
		if(this.ServerSocket == null) {
			System.out.println("Server socket isn't created, exiting...");
			System.exit(1);
		}
		
		//Creating Datagram (**TEMP**)
		while(true) {
			ClientPacket = new DatagramPacket(rawData, 0, rawData.length);
			
			System.out.println("Server listening at port " + this.ServerSocket.getLocalPort()+ " ...");
			try {
				this.ServerSocket.receive(ClientPacket);
			} catch (IOException e) {
				System.out.println("IOException at Socket.receive, exiting...");
				e.printStackTrace();
				System.exit(1);
			}
		
			//printPackageInfo(ClientPacket, false);
			
			int rInd1 = (int) rawData[0];
			int rInd2 = (int) rawData[1];
			
			if(rInd1 == 0 && rInd2 == 1) {
				clientT = new Thread(new Server(ClientPacket,1,rawData),"Thread #"+threadCounter);
				System.out.println("Thread #"+threadCounter+" Created");
				clientsT.put(threadCounter, clientT);
				threadCounter++;
				clientT.start();
			} else if (rInd1 == 0 && rInd2 == 2) {
				clientT = new Thread(new Server(ClientPacket,2,rawData),"Thread #"+threadCounter);
				System.out.println("Thread #"+threadCounter+" Created");
				clientsT.put(threadCounter, clientT);
				threadCounter++;
				clientT.start();
			} else {
				System.out.println("Invalid request, skiping...");
			}
			
			
			
			
		
		}
	}
	public void run() {	
		if(rType == 0) {
			Thread clientPro = null;
			if(this.ServerSocket == null) {
				System.out.println("Server socket isn't created, exiting...");
				System.exit(1);
			}
			
			//Creating Datagram (**TEMP**)
			while(true) {
				if(Thread.interrupted()) {
					System.out.println("Waiting all working thread are done...");
					boolean xx = true;
					Integer counter = 0;
					while(xx) {
						if(clientsT.get(counter) != null) {
							if(clientsT.get(counter).isAlive() == false) {
								counter++;
							} else {
								//System.out.println("Thread #" + counter+" Still working.");
							}
						} else {
							System.out.println("All threads are done, exiting...");
							xx=false;
						}
					}
					break;
				}
				ClientPacket = new DatagramPacket(rawData, 0, rawData.length);
				
				System.out.println("Server listening at port " + this.ServerSocket.getLocalPort()+ " ...");
				try {
					this.ServerSocket.receive(ClientPacket);
				} catch (IOException e) {
					System.out.println("IOException at Socket.receive, exiting...");
					e.printStackTrace();
					System.exit(1);
				}
			
				//printPackageInfo(ClientPacket, false);
				
				int rInd1 = (int) rawData[0];
				int rInd2 = (int) rawData[1];
				
				if(rInd1 == 0 && rInd2 == 1) {
					clientT = new Thread(new Server(ClientPacket,1,rawData),"Thread #"+threadCounter);
					System.out.println("Thread #"+threadCounter+" Created");
					clientsT.put(threadCounter, clientT);
					threadCounter++;
					clientT.start();
				} else if (rInd1 == 0 && rInd2 == 2) {
					clientT = new Thread(new Server(ClientPacket,2,rawData),"Thread #"+threadCounter);
					System.out.println("Thread #"+threadCounter+" Created");
					clientsT.put(threadCounter, clientT);
					threadCounter++;
					clientT.start();
				} else {
					System.out.println("Invalid request, skiping...");
				}	
			}
		} else {
		FILEUtil loadedFile = null;
		ACKPacket dataTrans = null;
		DataPacket dataPack = null;
		ErrorPacket E = null;
		byte[][] tempData = null;
		int run = 1;
		int blockCounter = 0;
		this.rawData = new byte[512];
		try {
			this.ServerSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rType == 2) { //Write Request
			tempData = new byte[512][];
			while (run == 1) {
				System.out.println("Working on Data# "+blockCounter);
				
				dataTrans = new ACKPacket(blockCounter);
				dataTrans.setDatagramPacket(this.ClientAddress, this.ClientPort);
				this.ClientPacket = dataTrans.getDatagramPacket();
				//printPackageInfo(this.ClientPacket, false);
				try {
					System.out.println("SENDING... ");
					this.ServerSocket.send(this.ClientPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println(this.ClientAddress.toString() + "" +this.ClientPacket.getPort());
				blockCounter++;
				
				this.ClientPacket = new DatagramPacket(this.rawData, this.rawData.length);
				try {
					this.ServerSocket.receive(this.ClientPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dataPack = new DataPacket(this.ClientPacket.getData(),this.ClientPacket.getLength());
				
				if(this.ClientPacket.getLength()<512) {
					run = -1;
				}
				
				tempData[blockCounter-1] = dataPack.getData();
				
				System.out.println(this.ClientPacket.getLength());			
			}
			tempData = Arrays.copyOfRange(tempData, 0, blockCounter);
			loadedFile = new FILEUtil(tempData);
			loadedFile.saveFile(dir+this.rFN);
			
		} else if (rType == 1) {
			try {
				loadedFile = new FILEUtil(dir+this.rFN);
			} catch(FileNotFoundException e) {
				//TESTING IF FILE EXISITS
				System.out.println("File not found, Responding ERROR...");
				E = new ErrorPacket(404, "File could not be found!");
				E.setDatagramPacket(this.ClientAddress, this.ClientPort);
				try {
					this.ServerSocket.send(E.getDatagramPacket());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			
			tempData = loadedFile.getData();
			
			while(blockCounter < tempData.length) {
				
				dataPack = new DataPacket(blockCounter, tempData[blockCounter]);
				System.out.println(tempData[blockCounter].length);
				dataPack.setDatagramPacket(this.ClientAddress, this.ClientPort);
				try {
					this.ServerSocket.send(dataPack.getDatagramPacket());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DatagramPacket ack = TFTPUtil.datagramPacket(4);
				dataTrans = new ACKPacket(rawData, rawData.length);
				try {
					this.ServerSocket.receive(ack);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				dataTrans = new ACKPacket(ack.getData(), ack.getLength());
				
				if(dataTrans.getIntBN() == blockCounter) {
					blockCounter++;
				} else {
					E = new ErrorPacket(409, "Conflict Error: Block number mismaatching!");
					E.setDatagramPacket(this.ClientAddress, this.ClientPort);
					try {
						this.ServerSocket.send(E.getDatagramPacket());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
			}
			
			
		}
		}
	}
	

	public static void main(String[] args) {
		Thread listener = new Thread(new Server(69),"Listener");
		listener.start();
		
	    String s = JOptionPane.showInputDialog("Shutdown server? Type 'quit':").toLowerCase();
	
	    if("quit".equals(s)) {
	        	listener.interrupt();
	    }
	}
	public static void printPackageInfo(DatagramPacket datagram, boolean sent) {


		String isSent = (sent) ? "sending " : "receiving ";
		String toFrom = (sent) ? "To " : "From ";

		System.out.println(isSent + "packet with the following information : ");
		System.out.println(toFrom + "Host: " + datagram.getAddress());

		System.out.println("Host port: " + datagram.getPort());
		System.out.println("Length: " + datagram.getLength());
		System.out.println("Contains: ");

		byte[] data = datagram.getData();

		String stringData = "";

		for (int i = datagram.getLength(); i > 0; i--) {
			
			int index = (-1 * (i - datagram.getLength()));
			
			System.out.println("byte " + index + " " + data[index]);

			if(index != 0 && index!=1) stringData += (char) data[index];
		}

		System.out.println("");
		System.out.println(stringData);
		System.out.println("");
	}
}
