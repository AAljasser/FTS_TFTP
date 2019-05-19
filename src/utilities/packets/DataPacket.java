package utilities.packets;
import utilities.ArrayUtil;
import utilities.BlockNum;

public class DataPacket extends Packet{
	
	private BlockNum blockNumber;
	private byte[] data;
	
	public DataPacket(int blockNumber, byte[] data) {
		this.blockNumber = new BlockNum(blockNumber);
		this.data = data;
		this.setPacket(constructPacket());
	}
	
	public DataPacket(byte[] packet) {
		this.setPacket(packet);
		blockNumber = extractBlockNumber(packet);
		data = extractData(packet);
	}

	public BlockNum getBlockNumber() {
		return blockNumber;
	}

	public byte[] getData() {
		return data;
	}

	public byte[] constructPacket() {
		byte[] id = new byte[2];
		byte[] bNumberArray = blockNumber.getByte();
		id[0] = 0;
		id[1] = 3;
		
		return ArrayUtil.makeSimpleArray(id, bNumberArray, data);
	}
	
	private BlockNum extractBlockNumber(byte[] packet) {
		byte[] blockNumberArray = ArrayUtil.subArray(packet, 2, 4);
		
		return new BlockNum(blockNumberArray);
	}
	
	private byte[] extractData(byte[] packet) {
		byte[] dataArray = ArrayUtil.subArray(packet, 4, packet.length);
		
		return dataArray;
	}
}