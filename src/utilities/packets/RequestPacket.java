package utilities.packets;
import utilities.ArrayUtil;
import utilities.Request;

public class RequestPacket extends Packet{
	
	private Request request;
	private String filename;
	private String mode;
	
	
	public RequestPacket(Request request, String filename, String mode) {
		this.setErrorPacket(null);
		this.request = request;
		this.filename = filename;
		this.mode = mode;	
		this.setID(request.getBytes());
		this.setPacket(constructPacket());
	}
	
	/*
	 * public RequestPacket(byte[] packet) { this.setPacket(packet); request =
	 * extractRequest(packet); filename = extractFilename(packet); mode =
	 * extractMode(packet); this.setID(extractID(packet));
	 * 
	 * }
	 */
	
	public RequestPacket(byte[] array, int length) throws Exception   {
		super(array, length);
		
		if(!this.isError()) {
			byte[] packet = ArrayUtil.subArray(array, 0, length);		
			if(validatePacket(packet)) {
				this.setPacket(packet);
				request = extractRequest(packet);
				filename = extractFilename(packet);
				mode = extractMode(packet);
				this.setID(extractID(packet));
			}
			
			else throw new Exception("Invalid RequestPacket format");
		}
		
	}
	
	private boolean validatePacket(byte[] packet)  {
		
		//request Packets must be at least 4 bytes (id = 2 bytes, separator = 2 bytes , mode= x bytes, filename = x bytes)
		//it really should be at least 12 bytes (because ... mode 5 bytes min, filename = 3 bytes min)
		//thinking that mode will always be 'octet'
		
		//packet length must be in the range 4 to 512
		if(packet.length < 4 || packet.length > 512) return false;
		
		//id bytes must be 0 and 1 or 2
		//packet[0] must be zero
		//packet[1] must be in the range 1 to 2
		else if(packet[0] != 0 || packet[1] < 1 || packet[1] > 2) return false;
		
		//check for filename and mode;
		//find the zeros second zero should be last byte on the array;	
		//if not packet is corrupted
		int secondZeroIndex = -1;
		boolean findFirst = false;
		
		for(int i = 2 ; i < packet.length ; i ++) {
			if(!findFirst && packet[i] == 0) findFirst = true;
			
			else if(findFirst && packet[i] == 0) { 
				secondZeroIndex = i;			
				break;
			}
		}
		
		//if the index of the second zero is not the last byte on the array ...
		if(secondZeroIndex != packet.length-1) return false;
		
		return true;
	}
	
	public Request getRequest() {
		return request;
	}

	public String getFilename() {
		return filename;
	}

	public String getMode() {
		return mode;
	}
	
	public  byte[] constructPacket() {
		byte[] separator = {0};
		
		return ArrayUtil.makeSimpleArray( getID(), filename.getBytes(), separator, mode.getBytes(), separator);
	}
	
	private Request extractRequest(byte[] packet) {
		byte[] requestBytes = ArrayUtil.subArray(packet, 0, 2);
		try {
			return new Request(requestBytes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}		
		
		return null;
	}
	
	private String extractFilename(byte[] packet) {
		String filename= "";
		
		byte[] subTemp = ArrayUtil.subArray(packet, 2, packet.length);
		
		int toIndex = 2 + ArrayUtil.indexOf(subTemp, 0);
		
		byte[] arrayFilename = ArrayUtil.subArray(packet, 2, toIndex);
		
		for(byte b : arrayFilename) filename += (char) b;
		
		return filename;
	}
	
	private String extractMode(byte[] packet) {
		String mode = "";
		
		byte[] subTemp = ArrayUtil.subArray(packet, 2, packet.length);
		
		int fromIndex = 3 + ArrayUtil.indexOf(subTemp, 0);	
		
		byte[] arrayMode =  ArrayUtil.subArray(packet, fromIndex, packet.length - 1);
		
		for(byte b : arrayMode) mode += (char) b;
		
		return mode;
	}
	
	private byte[] extractID(byte[] packet) {
		return ArrayUtil.subArray(packet, 0, 2);
	}

}