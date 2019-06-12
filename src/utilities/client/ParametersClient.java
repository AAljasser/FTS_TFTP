package utilities.client;

import java.util.Scanner;


public class ParametersClient {
	public enum RequestParameter{READ(1),WRITE(2); 
		
						private int id;
						
						private RequestParameter(int id) {
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
	private String request;
	private String filename;
	private String mode;
	
	public ParametersClient() {
	
	}
	
	public void getInfo() {
	
		System.out.println("Type of Request?");
		System.out.println("type: \n1 for read\n2 for write");
		int temp = scanner.nextInt();
		request = RequestParameter.values()[temp - 1].name();
		
		System.out.println("Enter filename (including its extension): ");
		filename = scanner.nextLine();
		
		System.out.println("Enter the mode");
		mode = scanner.nextLine();		
		
	}
	
	public String getFilename() {		
		return filename;
	}

	public String getMode() {
		return mode;
	}
	
	public int getRequestID() {
		return RequestParameter.valueOf(request).getID();
	}
	
	public String getRequestName() {
		return RequestParameter.valueOf(request).name();
	}
	
	public void setFilename(String name) {
		filename = name;
	}
	
	public void setRequestType(int id) {
		request = RequestParameter.values()[id - 1].name();
	}
	
	public void setMode(String m) {
		mode = m;
	}
	
	public void closeScanner() {
		scanner.close();
	}
	
	

}