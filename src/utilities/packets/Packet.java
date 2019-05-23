package utilities.packets;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import utilities.ArrayUtil;
import utilities.TFTPUtil;

public class Packet {
	
	private byte[] packet;
	private DatagramPacket datagramPacket;
	private byte[] id;
		
	public Packet() {
		
	}
	
	public Packet(DatagramPacket datagramPacket) {
		this.datagramPacket = datagramPacket;
		setPacket(datagramPacket.getData());
		setID(ArrayUtil.subArray(packet, 0, 2));		
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

}
