package utilities;
/**
 * This class represents a Request made by a client to a server
 * @author Jose Franco
 *
 */
public class Request {
	
//	public enum Types {WRITE, READ};
	
	private String type;
	private byte[] bytes;
	
	//Constructor
	public Request(String type) throws Exception {		
		if(!type.toUpperCase().equals("READ") && !type.toUpperCase().equals("WRITE")) throw new Exception();
		this.type = type.toUpperCase();	
		setBytesFromType();
	}
	
	public Request(byte[] bytes) throws Exception {
		if(bytes[0] != 0 && (bytes[1] != 1 || bytes[1] != 2)) throw new Exception();
		this.bytes = bytes;
		setTypeFromBytes();
	}
	
	/**	 
	 * @return byte[] array that represent the type of requests
	 */
	public byte[] getBytes() {
		return this.bytes;
	}
	/**
	 * @return String that represents the type of request
	 */
	public String getType() {
		return this.type;
	}
	
	private void setBytesFromType() {
		bytes = new byte[2];
		
		bytes[0] = 0;
		bytes[1] = (byte) ((type.equals("READ")) ? 1 : 2);
	}
	
	private void setTypeFromBytes() {
		type = (bytes[1] == 1) ? "READ" : "WRITE";
	}
}