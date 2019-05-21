package utilities;

/**
*  "Static" TFTPUtil class, contains methods that are used by all client, simulator and server
* @authors Jose Franco......... EVERYONE SHOULD WRITE HIS NAME HERE
*
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public final class TFTPUtil {

	private TFTPUtil() {
	}
	
	// METHODS TO CREATE DATAGRAMSOCKETS (method overload)
	/*
	 * @return DatagramSocket
	 */
	public static DatagramSocket datagramSocket() {
		try {
			return new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a datagramSocket using port "port"
	 * 
	 * @param port
	 * @return DatagramSocket
	 */
	public static DatagramSocket datagramSocket(int port) {
		try {
			return new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	// METHODS TO SEND RECEIVE DATAGRAMS

	/**
	 * Sends a datagramPacket packet using a datagramSocket socket
	 * 
	 * @param socket
	 * @param packet
	 * @param msj
	 */
	public static void send(DatagramSocket socket, DatagramPacket packet, String msj) {

		System.out.println(msj);

		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Receives a datagramPacket, storages the packet in parameter packet, uses
	 * parameter socket
	 * 
	 * @param socket
	 * @param packet
	 * @param msj
	 */
	public static void receive(DatagramSocket socket, DatagramPacket packet, String msj) {

		System.out.println(msj);

		try {
			// Block until a datagram is received via sendReceiveSocket.
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	//METHODS TO CREATE DATAGRAMPACKETS;
	public static DatagramPacket datagramPacket(int capacity) {
		byte[] array = new byte[capacity];
		
		return new DatagramPacket(array, capacity);
	}
	
	public static DatagramPacket datagramPacket(byte[] packet, int port) {
		try {
			DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, InetAddress.getLocalHost(), port);

			return datagramPacket;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static DatagramPacket datagramPacket(byte[] data, int length, InetAddress address, int port) {
		 return new DatagramPacket(data, length, address, port);
		
	}
	
	public static DatagramPacket  datagramPacket(DatagramPacket packet, int port) {
		return new DatagramPacket(packet.getData(), packet.getLength(), packet.getAddress(), port);

	}
	
}