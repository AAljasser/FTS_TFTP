package utilities.packets;
import utilities.ArrayUtil;
import utilities.Request;

public class RequestPacket extends Packet{
	
	private Request request;
	private String filename;
	private String mode;
	
	
	public RequestPacket(Request request, String filename, String mode) {
		this.request = request;
		this.filename = filename;
		this.mode = mode;	
		this.setID(request.getBytes());
		this.setPacket(constructPacket());
	}
	
	public RequestPacket(byte[] packet) {
		this.setPacket(packet);
		request = extractRequest(packet);
		filename = extractFilename(packet);
		mode = extractMode(packet);
		this.setID(extractID(packet));
		
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