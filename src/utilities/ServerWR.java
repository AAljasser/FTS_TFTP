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
		RequestPacket temp = null;
		try {
			temp = new RequestPacket(p.getData(), p.getLength());
		} catch (Exception e) {
			ErrorPacket err = new ErrorPacket(4, "Incorrect Packet");
			err.setDatagramPacket(this.cAdd, this.cPort);
			
			try {
				this.socket.send(err.getDatagramPacket());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(temp.isError()) {
			System.out.println("Error Code:"+temp.getErrorPacket().getIntBN()+ temp.getErrorPacket().getMsg());
			System.exit(1);
		}
		
		this.fileName = temp.getFilename();
		this.fileMode = temp.getMode();
	}

	@Override
	public void run() {
		byte[][] temp = new byte[65535][];
		byte[] rData = new byte[512];
		int bNum = 0;
		int run = 0;
		int tNum = 0;
		
		
		while (run == 0) {
			this.aPack = new ACKPacket(bNum);
			this.aPack.setDatagramPacket(this.cAdd, this.cPort);
			
			try {
				this.socket.send(this.aPack.getDatagramPacket());
				System.out.println("sending block#" + aPack.getIntBN());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.packet = new DatagramPacket(rData, rData.length);
			try {
				this.socket.setSoTimeout(500);
				this.socket.receive(this.packet);
				try {
					this.dPack = new DataPacket(this.packet.getData(),this.packet.getLength());
				} catch (Exception e1) {
					ErrorPacket err = new ErrorPacket(4, "Incorrect Packet");
					err.setDatagramPacket(this.cAdd, this.cPort);
					
					try {
						this.socket.send(err.getDatagramPacket());
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if(this.dPack.isError()) {
					System.out.println("Error Code:"+this.dPack.getErrorPacket().getIntBN()+ this.dPack.getErrorPacket().getMsg());
					System.exit(1);
				}
				
				/*if(this.dPack.getDatagramPacket().getPort() != this.cPort) {
					ErrorPacket err = new ErrorPacket(5, "Incorrect TID");
					err.setDatagramPacket(this.cAdd, this.cPort);
					
					try {
						this.socket.send(err.getDatagramPacket());
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					break;
				}*/
				
				if(bNum+1 == this.dPack.getIntBN()) {
					temp[bNum] = this.dPack.getData();
					bNum++;
					
					if(this.packet.getLength()<512) {
						run = -1;
						this.aPack = new ACKPacket(bNum);
						this.aPack.setDatagramPacket(this.cAdd, this.cPort);
						
						try {
							this.socket.send(this.aPack.getDatagramPacket());
							System.out.println("LAST PACKET SENT IS #: " + aPack.getIntBN());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				tNum=0;
			} catch (SocketTimeoutException e1) {
				System.out.println("ACK wait timed-out... retrying");
				tNum++;
				if(tNum > 50) {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			this.loadedFile = new FILEUtil(Arrays.copyOfRange(temp,0,bNum),dir+this.fileName,false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

