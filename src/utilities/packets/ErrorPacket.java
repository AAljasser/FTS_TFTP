package utilities.packets;
import utilities.ArrayUtil;

public class ErrorPacket extends Packet{
	
	private int errorCode;
	private String msg;

	public ErrorPacket(int errorCode, String msg) {
		this.errorCode = errorCode;
		this.msg = msg;
		this.setPacket(constructPacket());
	}
	
	public ErrorPacket(byte[] packet) {
		this.setPacket(packet);
		errorCode = this.extractErrorCode(packet);
		msg = this.extractMSG(packet);
	}
	
	public int getErrorCode() {
		return errorCode;
	}

	public String getMsg() {
		return msg;
	}
	
	public byte[]  constructPacket() {	
		byte[] id = new byte[2];
		byte[] end = {0};
		byte[] errCodeArray = ArrayUtil.intToBytes(errorCode);
		
		id[0] = 0;
		id[1] = 5;
		return  ArrayUtil.makeSimpleArray(id, errCodeArray, msg.getBytes(), end);
	}
	
	private int extractErrorCode(byte[] packet) {
		byte[] errCodeArray =  ArrayUtil.subArray(packet, 2, 4);
		
		return errCodeArray[0] * 128 + errCodeArray[1];
	}
	
	private String extractMSG(byte[] packet) {
		String msg = "";
		
		byte[] msgArray =  ArrayUtil.subArray(packet, 4, packet.length - 1);
		
		for(byte b : msgArray) msg += (char)b;
		
		return msg;
	}

}