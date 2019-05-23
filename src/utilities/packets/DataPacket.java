package utilities.packets;
import utilities.ArrayUtil;
import utilities.BlockNum;

public class DataPacket extends Packet{
	
	private BlockNum blockNumber;
	private byte[] data;
	
	public DataPacket(int blockNumber, byte[] data) {
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
	
	public DataPacket(byte[] array, int length) {
		byte[] packet = ArrayUtil.subArray(array, 0, length);
		this.setPacket(packet);
		blockNumber = extractBlockNumber(packet);
		data = extractData(packet);
		this.setID(extractID(packet));
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