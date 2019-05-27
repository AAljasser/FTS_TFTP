package utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

import program.Server;
import utilities.packets.*;

public class ServerRR extends Server {
	private String fileName;
	private String fileMode;
	private FILEUtil loadedFile;
	private DataPacket dPack;
	private ACKPacket aPack;

	public ServerRR(DatagramPacket p) {
		super(p);
		// TODO Auto-generated constructor stub
		RequestPacket temp = new RequestPacket(p.getData(), p.getLength());
		this.fileName = temp.getFilename();
		this.fileMode = temp.getMode();
	}

	@Override
	public void run() {
		
		
		
		try {
			this.loadedFile = new FILEUtil(dir+this.fileName);
		} catch (FileNotFoundException e) {
			ErrorPacket E = new ErrorPacket(404, "File could not be found!");
			E.setDatagramPacket(this.cAdd, this.cPort);
			try {
				this.socket.send(E.getDatagramPacket());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		
		
		byte[][] temp = this.loadedFile.getData();
		byte[] rData = new byte[4];
		int bNum = 0;
		
		while (bNum < temp.length) {
			dPack = new DataPacket(bNum,temp[bNum]);
			dPack.setDatagramPacket(this.cAdd, this.cPort);
			
			try {
				this.socket.send(dPack.getDatagramPacket());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.packet = new DatagramPacket(rData, rData.length);
			
			try {
				this.socket.setSoTimeout(500);
				this.socket.receive(this.packet);
				this.aPack = new ACKPacket(this.packet.getData(),this.packet.getLength());
				
				if(bNum == this.aPack.getIntBN()) bNum++;
			} catch (SocketException e1) {
				System.out.println("ACK receive timed-out... retrying");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

}
