package utilities.packets;
import java.net.DatagramPacket;
import java.net.InetAddress;

import utilities.ArrayUtil;
import utilities.TFTPUtil;

public class Packet {
	
	private byte[] packet;
	private DatagramPacket datagramPacket;
	private byte[] id;
	
	private boolean isError;	
	private ErrorPacket errorPacket;
		
	public Packet() {
		
	}
	
	public Packet(byte[] array, int length) {
		byte[] id = ArrayUtil.subArray(array, 0, 2);
		
		if(id[0] == 0 && id[1] == 5) {
			isError = true;
			try {				
				errorPacket = new ErrorPacket(array, length);
			} catch (Exception e) {
				System.out.println("Error in Class Packet when creating this.errorPacket");
				e.printStackTrace();
			}
		}
		else {
			errorPacket = null;
			isError = false;
		}
	}
		
	public byte[] getPacket() {
		return packet;
	}
	
	public void setPacket(byte[] packet) {
		this.packet = packet;
	}
		
	public DatagramPacket getDatagramPacket() {
		return datagramPacket;
	}
		
	public void setDatagramPacket(int port) {		
		this.datagramPacket = TFTPUtil.datagramPacket(packet, port);
	}
	
	
	public void setDatagramPacket(InetAddress address, int port) {
		this.datagramPacket = TFTPUtil.datagramPacket(packet, packet.length, address, port);
		
	}
		
	public void setID(byte[] id) {
		this.id = id;
	}
	
	public byte[] getID() {
		return id;
	}

	public ErrorPacket getErrorPacket() {
		return errorPacket;
	}
	
	public void setErrorPacket(ErrorPacket errorPacket) {
		 this.errorPacket = errorPacket;
	}
	
	public boolean isError() {
		return isError;
	}


}
