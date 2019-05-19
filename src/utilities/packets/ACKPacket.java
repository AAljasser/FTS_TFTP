package utilities.packets;
import utilities.ArrayUtil;
import utilities.BlockNum;

public class ACKPacket extends Packet{
	
	private BlockNum blockNumber;

	public ACKPacket(int blockNumber) {
		this.blockNumber = new BlockNum(blockNumber);
		this.setPacket(constructPacket());
	}
	
	public ACKPacket(byte[] packet) {
		this.setPacket(packet);
		blockNumber = extractBlockNumber(packet);		
	}
	
	

	public BlockNum getBlockNumber() {
		return blockNumber;
	}
	
	public byte[] constructPacket() {
		byte[] id = new byte[2];
		byte[] bNumberArray = blockNumber.getByte();
		
		id[0] = 0;
		id[1] = 4;
	
		return ArrayUtil.makeSimpleArray(id, bNumberArray);
	}
	
	private BlockNum extractBlockNumber(byte[] packet) {
		byte[] blockNumberArray = ArrayUtil.subArray(packet, 2, 4);
		
		return new BlockNum(blockNumberArray);
	}
}