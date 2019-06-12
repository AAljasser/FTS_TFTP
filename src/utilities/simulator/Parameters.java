package utilities.simulator;

import java.util.Scanner;

import javax.swing.JOptionPane;

public class Parameters {
	public enum Operation{NOTHING(0, "nothing"), DELAY(1, "delay"), DUPLICATE(2, "duplicate"),  LOST(3, "lost"),
		OPCODEERROR(4, "op code error"), BLOCKNUM(5, "block num error"), IDERROR(6, "id error");
		
						private int id;
						private String name;
						
						private Operation(int id, String name) {
							this.id = id;
							this.name = name;
						}
						
						public int getID() {
							return id;
						}
						
						public String getName() {
							return name;
						}
	};

	public enum PacketType{DATAPACKET(1, "datapacket"), ACKPACKET(2, "ackpacket");
		
		private int id;
		private String name;
		private PacketType(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public int getID() {
			return id;
		}
		
		public String getName() {
			return name;
		}
};
	
	private Scanner scanner = new Scanner(System.in);
	private int from;
	private int to;
	private Operation operation;
	private PacketType packetType;
	
	public Parameters() {
	
	}
	
	public void getInfo() {
	
		System.out.println("Which operation you want to perfom?");
		System.out.println("type: \n0 for no operation \n1 for delay\n2 for duplicate\n3 for lose\n4 for op code error\n5 for blockNum error\n6 for id error");
		operation = Operation.values()[scanner.nextInt()];
		
		System.out.println("What type of packets you want to affect?");
		System.out.println("type:\n1 for DataPackets\n2 for ACKPackets");
		packetType = PacketType.values()[scanner.nextInt() - 1];
		
		System.out.println("From which packet you want to start?");
		from = scanner.nextInt();
		
		System.out.println("In what packet you want to stop?");
		to = scanner.nextInt();
		
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
	
	public String getPacketTypeName() {
		return packetType.getName();
	}

	public String getOperationName() {
		return operation.getName();
	}
	
	public int getOperationID() {
		return operation.getID();
	}
	
	public void closeScanner() {
		scanner.close();
	}

}