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
			byteValue[1] = (byte) (num >>7);
			byteValue[0] = (byte) (num & 0b1111111);
		}
	}
	public BlockNum(byte[] num) {
		if(num.length > 2 || num.length < 1) {
			System.out.println("Array too small...");
		} else {
			byteValue = num;
			intValue = (int)(byteValue[1]<<7)+(int)byteValue[0];
		}
	}
	public int getInt() {
		return intValue;
	}
	public byte[] getByte() {
		return byteValue;
	}
	
}
