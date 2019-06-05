package utilities.packets;
import utilities.ArrayUtil;
import utilities.BlockNum;

public class DataPacket extends Packet{
	
	private BlockNum blockNumber;
	private byte[] data;
	
	public DataPacket(int blockNumber, byte[] data) {
		this.setErrorPacket(null);
		this.blockNumber = new BlockNum(blockNumber);
		this.data = data;
		this.setID(new byte[] {0,3});
		this.setPacket(constructPacket());
	}
	
	/*
	 * public DataPacket(byte[] packet) { this.setPacket(packet); blockNumber =
	 * extractBlockNumber(packet); data = extractData(packet);
	 * this.setID(extractID(packet)); }
	 */
	
	public DataPacket(byte[] array, int length) throws Exception {
		super(array, length);
		
		if(!this.isError()) {
			byte[] packet = ArrayUtil.subArray(array, 0, length);
			
			if(validatePacket(packet)) {
				this.setPacket(packet);
				blockNumber = extractBlockNumber(packet);
				data = extractData(packet);
				this.setID(extractID(packet));
			}
			
			else throw new Exception("Invalid DataPacket format");
		}
	}
	
	private boolean validatePacket(byte[] packet)  {
		
		//data packets min length is 4 bytes(id = 2 bytes, blockNum = 2 bytes, data = 0 bytes), max length is 512 bytes
		//packet.length must be in the range 4 to 512
		if(packet.length < 4 || packet.length > 512) return false;
		
		//check for the ID, the first two bytes must be 0 and 3
		else if(packet[0] != 0 || packet[1] != 3) return false;
		
		//we decide not to check for block numbers, remove comments to check for them;
		//else if(packet[2] / 128 >= 1 || packet[2] / 128 <= -1) return false;
		//else if(packet[3] / 128 >= 1 || packet[3] / 128 <= -1) return false;
		
		//there's no way to check for data correctness, we can only look at the length of the array (must be >= 4 and <=512)
		
		return true;
	}

	public BlockNum getBlockNumber() {
		return blockNumber;
	}
	public int getIntBN() {
		return blockNumber.getInt();
	}
	
	public byte[] getBytesBN() {
		return blockNumber.getByte();				
	}

	public byte[] getData() {
		return data;
	}

	public byte[] constructPacket() {		
		byte[] bNumberArray = blockNumber.getByte();		
		
		return ArrayUtil.makeSimpleArray(getID(), bNumberArray, data);
	}
	
	private BlockNum extractBlockNumber(byte[] packet) {
		byte[] blockNumberArray = ArrayUtil.subArray(packet, 2, 4);
		
		return new BlockNum(blockNumberArray);
	}
	
	private byte[] extractData(byte[] packet) {
		byte[] dataArray = ArrayUtil.subArray(packet, 4, packet.length);
		
		return dataArray;
	}
	
	private byte[] extractID(byte[] packet) {
		return ArrayUtil.subArray(packet, 0, 2);
	}
}