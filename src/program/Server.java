package program;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

import java.lang.*;
import utilities.*;
import utilities.packets.*;

public class Server implements Runnable {
	//Defaults
	protected static String dir = "C:\\Jose\\Java\\files\\save\\";
	
	protected DatagramSocket socket;
	protected DatagramPacket packet;
	protected InetAddress cAdd = null;
	protected int cPort;
	
	
	public Server(int port) {
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	public Server(DatagramPacket p) {
		try {
			this.socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.cAdd = p.getAddress();
		this.cPort = p.getPort();
	}
	
	@Override
	public void run() {
		Map<Integer, Thread> clientsT = new HashMap<Integer, Thread>();
		Thread clientThread = null;
		byte[] rData = new byte[512];
		int counter = 0;
		
		while(true) {
			 this.packet = new DatagramPacket(rData, 0, rData.length);
			 System.out.println("Waiting for request at port 69.");
			 try {
				this.socket.receive(this.packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			 
			int rInd1 = (int) rData[0];
			int rInd2 = (int) rData[1];
			 
			 if(rInd1 == 0 && rInd2 == 1) {
				 clientThread = new Thread(new ServerRR(this.packet),"Thread_"+counter);
				 clientsT.put(counter, clientThread);
				 clientThread.start();
				 counter++;
			 } else if (rInd1 == 0 && rInd2 == 2) {
				 clientThread = new Thread(new ServerWR(this.packet),"Thread_"+counter);
				 clientsT.put(counter, clientThread);
				 clientThread.start();
				 counter++;
			 } else if (rInd1 == 5 && rInd2 == 6) {
				 System.out.println("Waiting all working thread are done...");
				 boolean xx = true;
					Integer cc = 0;
					while(xx) {
						if(clientsT.get(cc) != null) {
							if(clientsT.get(cc).isAlive() == false) {
								cc++;
							} else {
								//System.out.println("Thread #" + counter+" Still working.");
							}
						} else {
							System.out.println("All threads are done, exiting...");
							xx=false;
						}
					}
					break;
			 } else {
				 System.out.println("Invalid request, skiping...");
			 }
			 
			 
		}
		
	}
	
	
	public static void main(String args[]) {
		Thread serverThread = new Thread(new Server(69),"ServerThread");
		serverThread.start();
		
		Object[] options = {"Quit"};
		JOptionPane.showOptionDialog(null,
                "Stop server","Server Interface",
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
		DatagramPacket p=null;
		DatagramSocket s=null;
		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		byte[] d = new byte[10];
		d[0] = 0b00000101;
		d[1] = 0b00000110;
		
		try {
			p = new DatagramPacket(d,d.length,InetAddress.getLocalHost(),69);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		try {
			s.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}