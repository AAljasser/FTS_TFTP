package utilities.packets;
import utilities.ArrayUtil;
import utilities.BlockNum;

public class ACKPacket extends Packet{
	
	private BlockNum blockNumber;

	public ACKPacket(int blockNumber) {
		
		this.setErrorPacket(null);
		this.blockNumber = new BlockNum(blockNumber);
		this.setID(new byte[] {0,4});
		this.setPacket(constructPacket());
		
	}
	
	/*
	 * public ACKPacket(byte[] packet) { this.setPacket(packet); blockNumber =
	 * extractBlockNumber(packet); this.setID(extractID(packet)); }
	 */
	
	public ACKPacket(byte[] array, int length) throws Exception   {
		super(array, length);
		
		if(!this.isError()) {
			byte[] packet = ArrayUtil.subArray(array, 0, length);
			
			if(validatePacket(packet)) {
				this.setPacket(packet);
				blockNumber = extractBlockNumber(packet);	
				this.setID(extractID(packet));
			}
			
			else  throw new Exception("Invalid ACKPacket format");
		}
	}
	
	private boolean validatePacket(byte[] packet)  {
		
		//length of ACK PACKETS must be 4 (id = 2 bytes , block number = 2 bytes)
		//packet.length must be in the range 4 to 512
		if(packet.length != 4) return false;
		
		//ID bytes must be 0 and 4
		else if(packet[0] != 0 || packet[1] != 4) return false;
		
		//we decide not to check for block numbers, remove comments to check for them;
		//else if(packet[2] / 128 >= 1 || packet[2] / 128 <= -1) return false;
		//else if(packet[3] / 128 >= 1 || packet[3] / 128 <= -1) return false;
		
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