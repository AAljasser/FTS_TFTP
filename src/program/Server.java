package program;
import java.io.*;
import java.net.*;
import java.lang.Thread;
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
	//*** TEMP
	private DatagramSocket ServerSocket = null;
	
	
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
		RequestPacket temp = new RequestPacket(rd);
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
		
			printPackageInfo(ClientPacket, false);
			
			int rInd1 = (int) rawData[0];
			int rInd2 = (int) rawData[1];
			
			if(rInd1 == 0 && rInd2 == 1) {
				Thread clientT = new Thread(new Server(ClientPacket,1,rawData),"Thread #"+threadCounter);
				System.out.println("Thread #"+threadCounter+" Created");
				threadCounter++;
				clientT.start();
			} else if (rInd1 == 0 && rInd2 == 2) {
				Thread clientT = new Thread(new Server(ClientPacket,2,rawData),"Thread #"+threadCounter);
				System.out.println("Thread #"+threadCounter+" Created");
				threadCounter++;
				clientT.start();
			} else {
				System.out.println("Invalid request, skiping...");
			}
			
			
			
			
		
		}
	}
	public void run() {
		ACKPacket dataTrans = null;
		DataPacket dataPack = null;
		byte[][] tempData = new byte[512][508];;
		int run = 1;
		int blockCounter = 0;
		if(rType == 2) { //Write Request
			try {
				this.ServerSocket = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while (run == 1) {
				System.out.println("Working on Data# "+blockCounter);
				System.out.println(this.ClientAddress.toString() + "" +this.ClientPort);
				dataTrans = new ACKPacket(blockCounter);
				dataTrans.setDatagramPacket(this.ClientAddress, this.ClientPort);
				this.ClientPacket = dataTrans.getDatagramPacket();
				try {
					System.out.println("SENDING... ");
					this.ServerSocket.send(this.ClientPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				blockCounter++;
				this.rawData = new byte[512];
				this.ClientPacket = new DatagramPacket(this.rawData, this.rawData.length);
				try {
					this.ServerSocket.receive(this.ClientPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dataPack = new DataPacket(this.ClientPacket.getData());
				
				if(this.ClientPacket.getLength()<512) {
					run = -1;
				}
				
				tempData[blockCounter-1] = dataPack.getData();
				
				System.out.println(this.ClientPacket.getLength());
				
				dataPack = new DataPacket(this.ClientPacket.getData());
				
				
			}
		}
	}
	

	public static void main(String[] args) {
		Server run = new Server(60300);
		run.Start();

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
