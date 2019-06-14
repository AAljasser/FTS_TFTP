package utilities;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import program.Server;
import utilities.packets.ACKPacket;
import utilities.packets.DataPacket;
import utilities.packets.ErrorPacket;
import utilities.packets.RequestPacket;

public class ServerWR extends Server {
	private FileChannel loadedChannel;
	private File loadedFile;
	private String fileName;
	private String fileMode;
	//private FILEUtil loadedFile;
	private DataPacket dPack;
	private ACKPacket aPack;
	private boolean err = false;
	

	public ServerWR(DatagramPacket p, boolean t) {
		super(p,t);
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
		
		this.loadedFile = new File(dir+this.fileName);
		
		if(this.loadedFile.exists()) {
			
			try {
				this.loadedChannel = FileChannel.open(this.loadedFile.toPath(), StandardOpenOption.WRITE);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				try {
					this.loadedChannel.tryLock();
					ErrorPacket err = new ErrorPacket(6, "File Already Exists");
					err.setDatagramPacket(this.cAdd, this.cPort);
					
					try {
						this.socket.send(err.getDatagramPacket());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println("Error 6: File Already Exists");
					this.err = true;
					this.loadedChannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (OverlappingFileLockException e) {
				try {
					this.loadedChannel.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				ErrorPacket err = new ErrorPacket(2, "Access violation");
				err.setDatagramPacket(this.cAdd, this.cPort);
				
				System.out.println("Error 2: Access violation");
				try {
					this.socket.send(err.getDatagramPacket());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
				//TODO Create Error Packet
			}
			
			
			
			//TODO File Exists ERROR
			ErrorPacket err = new ErrorPacket(6, "File Already Exists");
			err.setDatagramPacket(this.cAdd, this.cPort);
			
			try {
				this.socket.send(err.getDatagramPacket());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println("Error 6: File Already Exists");
			this.err = true;
		}
		if(!this.err) {
		try {
			this.loadedChannel = FileChannel.open(this.loadedFile.toPath(),StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			try {
				this.loadedChannel.tryLock();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (OverlappingFileLockException e) {
			try {
				this.loadedChannel.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			ErrorPacket err = new ErrorPacket(2, "Access violation");
			err.setDatagramPacket(this.cAdd, this.cPort);
			
			
			try {
				this.socket.send(err.getDatagramPacket());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			System.out.println("Error 2: Access violation");
			this.err = true;
			//TODO Create Error Packet
		}}
	}
	
	@Override
	public void run() {
		byte[] rData = new byte[512];
		
		int bNum = 0;
		int run = 0;
		int tNum = 0;
		
		if(this.err) {
			run = -1;
		}
		
		while (run ==0) {
			this.aPack = new ACKPacket(bNum);
			this.aPack.setDatagramPacket(this.cAdd,this.cPort);
			
			if(verbose) {
				System.out.println("Sending to client ACK #"+bNum);
			}
			
			try {
				this.socket.send(this.aPack.getDatagramPacket());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.packet = new DatagramPacket(rData,rData.length);
			try {
				this.socket.setSoTimeout(500);
				this.socket.receive(this.packet);
				
				
				
				try {
					this.dPack = new DataPacket(this.packet.getData(),this.packet.getLength());
				} catch (Exception e) {
					this.loadedChannel.close();
					this.loadedFile.delete();
					ErrorPacket err = new ErrorPacket(4, e.getMessage());
					err.setDatagramPacket(this.cAdd, this.cPort);
					
					try {
						this.socket.send(err.getDatagramPacket());
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					// TODO Auto-generated catch block
					System.out.println("Error 4: "+e.getMessage());
					return;
				}
				
				if(this.dPack.isError()) {
					System.out.println("Error Code:"+this.dPack.getErrorPacket().getIntBN()+ this.dPack.getErrorPacket().getMsg());
					return;
				}
				
				if(verbose) {
					System.out.println("Recieved from client Data #"+this.dPack.getIntBN());
					
				}
				
				if(this.packet.getPort() != this.cPort) {
					ErrorPacket E = new ErrorPacket(5, "Unknown transfer ID");
					System.out.println("Unknown transfer ID");
					E.setDatagramPacket(this.packet.getAddress(), this.packet.getPort());
					
					this.socket.send(E.getDatagramPacket());
				} else if(bNum+1 == this.dPack.getIntBN()) {
					ByteBuffer saving = ByteBuffer.wrap(this.dPack.getData());
					
					
					if(this.loadedFile.getParentFile().getFreeSpace() < this.dPack.getData().length) {
						
						ErrorPacket err = new ErrorPacket(3, "Disk Full");
						err.setDatagramPacket(this.cAdd, this.cPort);
						this.socket.send(err.getDatagramPacket());
						this.loadedChannel.close();
						this.loadedFile.delete();
						return;
					}
					
					this.loadedChannel.write(saving);
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
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				tNum++;
				if(tNum > 50) {
					System.out.println("Closing Connetion, Timeout limit excceded...");
					try {
						this.loadedChannel.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					this.loadedFile.delete();
					break;
				}
				System.out.println("Client ACK respond timedout...");
				//e.printStackTrace();
			}
			
		}
		try {
			if(this.loadedChannel.isOpen())
			this.loadedChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	/*@Override
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
	}*/

}