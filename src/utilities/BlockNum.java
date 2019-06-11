/*
 * Class BlockNum:
 * 	Turns int into 14 bit and store it in a 2 byte array that can be used for block number
 * 
 * Methods:
 * 	BlockNum(int) --> Turns int to 2 byte array
 * 	BlockNum(byte[]) --> Turns 2 byte array to int
 */
package utilities;

public class BlockNum {
	static byte[] byteValue;
	static int intValue = -1;
	public BlockNum(int num) {
		if(num >= 65535 || num < 0) {
			System.out.println("Out of range, 0 <= num <= 65535...");

		} else {
			byteValue = new byte[2];
			intValue = num;
			byteValue[1] = (byte) (num >>> 8);
			byteValue[0] = (byte) (num & 0xff);
		}
	}
	public BlockNum(byte[] num) {
		if(num.length > 2 || num.length < 1) {
			System.out.println("Array too small...");
		} else {
			byteValue = num;
			intValue = (int)(((byteValue[1] & 0xff) <<8)| (byteValue[0] & 0xff));
		}
	}
	public int getInt() {
		return intValue;
	}
	public byte[] getByte() {
		return byteValue;
	}
	
	public static void main(String args[]) {
		BlockNum x = null;
		BlockNum y = null;
		
		for(int i =0; i<65000;i++) {
			x = new BlockNum(i);
			y = new BlockNum(x.getByte());
			if(x.getInt() != y.getInt()) {
				System.out.println("Error at: "+ i);
			}
		}
		
	}
}
