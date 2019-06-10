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
	private boolean err = false;
	

	public ServerWR(DatagramPacket p) {
		super(p);
		RequestPacket temp = null;
		try {
			temp = new RequestPacket(p.getData(), p.getLength());
		} catch (Exception e) {
			ErrorPacket err = new ErrorPacket(4, "Incorrect Packet");
			err.setDatagramPacket(this.cAdd, this.cPort);
			
			try {
				this.socket.send(err.getDatagramPacket());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("Error 4: Packet Opecode Couldn't be recognized");
			this.err = true;
		}
		
		if(temp.isError()) {
			System.out.println("Error Code: "+temp.getErrorPacket().getIntBN()+ temp.getErrorPacket().getMsg());
			this.err = true;
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

		if(this.err) {
			run = -1;
		}
		
		
		while (run == 0) {
			this.aPack = new ACKPacket(bNum);
			this.aPack.setDatagramPacket(this.cAdd, this.cPort);
			
			if(verbose) {
				System.out.println("Sending to client ACK #"+bNum);
			}
			
			try {
				this.socket.send(this.aPack.getDatagramPacket());
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
					ErrorPacket err = new ErrorPacket(4, "Incorrect Data Packet");
					err.setDatagramPacket(this.cAdd, this.cPort);
					
					try {
						this.socket.send(err.getDatagramPacket());
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					// TODO Auto-generated catch block
					System.out.println("Error 4: Packet Received is unrecognizable");
					return;
				}
				
				
				if(this.dPack.isError()) {
					System.out.println("Error Code:"+this.dPack.getErrorPacket().getIntBN()+ " " + this.dPack.getErrorPacket().getMsg());
					return;
				}
				
				if(this.packet.getPort() != this.cPort) {
					ErrorPacket E = new ErrorPacket(5, "Error 5: Unknown transfer ID");
					System.out.println("Error 5: Unknown transfer ID");
					E.setDatagramPacket(this.packet.getAddress(), this.packet.getPort());
					
					this.socket.send(E.getDatagramPacket());

				} else if(bNum+1 == this.dPack.getIntBN()) {
					if(verbose) {
						System.out.println("Recieved from client Data #"+this.dPack.getIntBN());
						
					}
					temp[bNum] = this.dPack.getData();
					bNum++;
					
					if(this.packet.getLength()<512) {
						run = -1;
						this.aPack = new ACKPacket(bNum);
						this.aPack.setDatagramPacket(this.cAdd, this.cPort);
						
						try {
							this.socket.send(this.aPack.getDatagramPacket());
							if(this.verbose) System.out.println("LAST PACKET SENT IS #: " + aPack.getIntBN());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				tNum=0;
			} catch (SocketTimeoutException e1) {
				if(this.verbose) System.out.println("ACK wait timed-out... retrying");
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