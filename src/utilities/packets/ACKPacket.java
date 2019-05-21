package utilities.packets;
import utilities.ArrayUtil;
import utilities.BlockNum;

public class ACKPacket extends Packet{
	
	private BlockNum blockNumber;

	public ACKPacket(int blockNumber) {
		this.blockNumber = new BlockNum(blockNumber);
		this.setID(new byte[] {0,4});
		this.setPacket(constructPacket());
	}
	
	public ACKPacket(byte[] packet) {
		this.setPacket(packet);
		blockNumber = extractBlockNumber(packet);	
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
	
	public byte[] constructPacket() {
		byte[] bNumberArray = blockNumber.getByte();		
	
		return ArrayUtil.makeSimpleArray(getID(), bNumberArray);
	}
	
	private BlockNum extractBlockNumber(byte[] packet) {
		byte[] blockNumberArray = ArrayUtil.subArray(packet, 2, 4);
		
		return new BlockNum(blockNumberArray);
	}
	
	private byte[] extractID(byte[] packet) {
		return ArrayUtil.subArray(packet, 0, 2);
	}
	
	
}