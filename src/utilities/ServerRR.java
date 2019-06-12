package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

import program.Server;
import utilities.packets.*;

public class ServerRR extends Server {
	private String fileName;
	private String fileMode;
	private FILEUtil loadedFile;
	private DataPacket dPack;
	private ACKPacket aPack;
	private boolean err = false;

	public ServerRR(DatagramPacket p) {
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
				e1.printStackTrace();
			}
			System.out.println("Error 4: Packet Opecode Couldn't be recognized");
			this.err = true;
		}
		
		if(temp.isError()) {
			System.out.println("Error Code:"+temp.getErrorPacket().getIntBN()+ temp.getErrorPacket().getMsg());
			this.err = true;
		}
		
		this.fileName = temp.getFilename();
		this.fileMode = temp.getMode();
	}

	@Override
	public void run() {
		
		if(this.err) {
			return;
		}
		
		FileChannel check = null;
		File s = new File(dir+this.fileName);
		System.out.println(dir+this.fileName);
		
		try {
			check= FileChannel.open(s.toPath(),StandardOpenOption.WRITE,StandardOpenOption.READ);
		} catch (NoSuchFileException ex){ 
			ErrorPacket E = new ErrorPacket(1, "File not found!");
			E.setDatagramPacket(this.cAdd, this.cPort);
			try {
				this.socket.send(E.getDatagramPacket());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("File Named: "+this.fileName+" Could not be found.");
			return;
			
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		try {
			check.tryLock();
			check.close();
		} catch (OverlappingFileLockException e) {
			ErrorPacket err = new ErrorPacket(2, "Access violation");
			err.setDatagramPacket(this.cAdd, this.cPort);
			
			System.out.println("Error 2: Access violation");
			try {
				this.socket.send(err.getDatagramPacket());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		try {
			this.loadedFile = new FILEUtil(dir+this.fileName);
		} catch (FileNotFoundException e) {
			ErrorPacket E = new ErrorPacket(1, "File not found!");
			E.setDatagramPacket(this.cAdd, this.cPort);
			try {
				this.socket.send(E.getDatagramPacket());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("File Named: "+this.fileName+" Could not be found.");
			return;
		}
		
		
		byte[][] temp = this.loadedFile.getData();
		byte[] rData = new byte[4];
		int bNum = 0;
		int tNum = 0;
		
		while (bNum < temp.length) {
			dPack = new DataPacket(bNum,temp[bNum]);
			dPack.setDatagramPacket(this.cAdd, this.cPort);
			
			try {
				if(this.verbose) System.out.println("Sending DataPacket #"+dPack.getIntBN());
				this.socket.send(dPack.getDatagramPacket());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.packet = new DatagramPacket(rData, rData.length);
			
			try {
				this.socket.setSoTimeout(500);
				this.socket.receive(this.packet);
				try {
					
					this.aPack = new ACKPacket(this.packet.getData(),this.packet.getLength());
					if(this.verbose) System.out.println("Received ACKPacket #"+this.aPack.getIntBN());
				} catch (Exception e) {
					ErrorPacket err = new ErrorPacket(4, "Incorrect Ack Packet");
					err.setDatagramPacket(this.cAdd, this.cPort);
					
					this.socket.send(err.getDatagramPacket());
					
					System.out.println("Error 4: Packet Received is unrecognizable");
					return;
				}
				
				if(this.aPack.isError()) {
					System.out.println("Error Code:"+this.dPack.getErrorPacket().getIntBN()+ this.dPack.getErrorPacket().getMsg());
					return;
				}
				
				if(this.packet.getPort() != this.cPort) {
					ErrorPacket E = new ErrorPacket(5, "Error 5: Unknown transfer ID");
					System.out.println("Error 5: Unknown Source!");
					E.setDatagramPacket(this.packet.getAddress(), this.packet.getPort());
					
					this.socket.send(E.getDatagramPacket());
				} else if(bNum == this.aPack.getIntBN()) {
					bNum++;
				}
				
				if(bNum == temp.length && this.verbose) {
					System.out.println("LAST PACKET SENT IS #: " + aPack.getIntBN());
				}
				tNum = 0;
			} catch (SocketTimeoutException e1) {
				System.out.println("ACK receive timed-out... retrying");
				tNum++;
				if(tNum > 50) {
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

}