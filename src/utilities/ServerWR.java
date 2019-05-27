package utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import program.Server;
import utilities.packets.ACKPacket;
import utilities.packets.DataPacket;
import utilities.packets.ErrorPacket;
import utilities.packets.RequestPacket;

public class ServerWR extends Server {
	private String fileName;
	private String fileMode;
	private FILEUtil loadedFile;
	private DataPacket dPack;
	private ACKPacket aPack;

	public ServerWR(DatagramPacket p) {
		super(p);
		// TODO Auto-generated constructor stub
		RequestPacket temp = new RequestPacket(p.getData(), p.getLength());
		this.fileName = temp.getFilename();
		this.fileMode = temp.getMode();
	}

	@Override
	public void run() {
		byte[][] temp = new byte[1024][];
		byte[] rData = new byte[512];
		int bNum = 0;
		int run = 0;
		int tNum = 0;
		
		
		while (run == 0) {
			this.aPack = new ACKPacket(bNum);
			this.aPack.setDatagramPacket(this.cAdd, this.cPort);
			
			try {
				this.socket.send(this.aPack.getDatagramPacket());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.packet = new DatagramPacket(rData, rData.length);
			try {
				this.socket.setSoTimeout(500);
				this.socket.receive(this.packet);
				this.dPack = new DataPacket(this.packet.getData(),this.packet.getLength());
				
				if(bNum+1 == this.dPack.getIntBN()) {
					temp[bNum] = this.dPack.getData();
					bNum++;
					if(this.packet.getLength()<512) {
						run = -1;
					}
				}
				tNum=0;
			} catch (SocketTimeoutException e1) {
				System.out.println("ACK wait timed-out... retrying");
				tNum++;
				if(tNum > 5) {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.loadedFile = new FILEUtil(Arrays.copyOfRange(temp,0,bNum));
		this.loadedFile.saveFile(dir+this.fileName);
	}

}

