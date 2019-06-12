package utilities.simulator;

import java.util.Scanner;

import javax.swing.JOptionPane;

public class ParametersSimulator {
	public enum Operation{NOTHING(0),DELAY(1), DUPLICATE(2),  LOST(3),
		OPCODEERROR(4), BLOCKNUM(5), IDERROR(6);
		
						private int id;
						private String name;
						
						private Operation(int id) {
							this.id = id;
							
						}
						
						public int getID() {
							return id;
						}
						
	};

	public enum PacketType{DATAPACKET(1), ACKPACKET(2);
		
		private int id;
		
		private PacketType(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
		
};
	
	private Scanner scanner = new Scanner(System.in);
	private int from;
	private int to;
	private Operation operation;
	private PacketType packetType;
	
	public ParametersSimulator() {
	
	}
	
	public void getInfo() {
	
		System.out.println("Which operation you want to perfom?");
		System.out.println("type: \n0 Nothing\n1 for delay\n2 for duplicate\n3 for lose\n4 for op code error\n5 for blockNum error\n6 for id error");
		operation = Operation.values()[scanner.nextInt()];
		
		if(operation.getID() == 0) {
			packetType = PacketType.values()[0];
			from = 1;
			to = 1;
		}
		else if(operation.getID() == 4 || operation.getID() == 5) {
			System.out.println("What type of packets you want to affect?");
			System.out.println("type:\n1 for DataPackets\n2 for ACKPackets");
			packetType = PacketType.values()[scanner.nextInt() - 1];
			
			System.out.println("In what packet you want to make the error?");
			from = scanner.nextInt();
			
			to = from;
		}
		else {
			System.out.println("What type of packets you want to affect?");
			System.out.println("type:\n1 for DataPackets\n2 for ACKPackets");
			packetType = PacketType.values()[scanner.nextInt() - 1];
			
			System.out.println("From which packet you want to start?");
			from = scanner.nextInt();
			
			System.out.println("In what packet you want to stop?");
			to = scanner.nextInt();
		}
		
	}
	
	public int getFrom() {		
		return from;
	}

	public int getTo() {
		return to;
	}
	
	public int getPacketTypeID() {
		return packetType.getID();
	}
	

	
	public int getOperationID() {
		return operation.getID();
	}
	
	public void closeScanner() {
		scanner.close();
	}
	
	public void setOperation(int opID) {
		operation = Operation.values()[opID];
	}
	
	public void setPacket(int pID) {
		packetType = PacketType.values()[pID - 1];
	}
	
	public void setFrom(int val) {
		from = val;
	}
	
	public void setTo(int val) {
		to = val;
	}
	
	public String getOperationName() {
		return operation.name();
	}
	
	public String getPacketName() {
		return packetType.name();
	}

}