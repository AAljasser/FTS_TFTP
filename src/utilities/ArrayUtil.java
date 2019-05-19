package utilities;
import java.util.Arrays;

public class ArrayUtil {
	
	private ArrayUtil(){}
	
	//Method to break an array into subArrays
	public static byte[][] makeBidimensional(byte[] original, int subLength) {

		int mainArrayLength = (int) Math.ceil((double) original.length / subLength);

		byte[][] mainArray = new byte[mainArrayLength][];

		int fromIndex = 0;
		int toIndex = 0;
		int remainder = original.length;

		for (int i = 0; i < mainArrayLength; i++) {

			if (remainder < subLength) {

				toIndex += remainder;

				//mainArray[i] = new byte[remainder];

				mainArray[i] = Arrays.copyOfRange(original, fromIndex, toIndex);
			} else {
				mainArray[i] = new byte[subLength];

				toIndex += subLength;

				mainArray[i] = Arrays.copyOfRange(original, fromIndex, toIndex);

				remainder -= subLength;
				fromIndex += subLength;
			}
		}

		return mainArray;
	}
	
	//Method to make a bidimensional array into one dimension array
	public static byte[] makeSimpleArray(byte[]... subArrays) {
		int length = 0;
		int index = 0;
		
		for(byte[] subArray : subArrays) length += subArray.length;
		
		byte[] dataArray = new byte[length];
		
		for(byte[] subArray : subArrays) {
			System.arraycopy(subArray, 0, dataArray, index, subArray.length);
			
			index += subArray.length;
		}
		
		return dataArray;		
	}
	
	//pushes an array into a bidimensional array
	public static byte[][] pushBidimensional(byte[][] container, byte[] subArray){
		byte[][] array = new byte[container.length + 1][];
		
		for(int i = 0; i < array.length; i++) {
			if(i == array.length - 1) array[i] = subArray;
			
			else array[i] = container[i];
		}
		
		return array;
	}
	
	//int to array of bytes; WE ONLY NEED byte[2]
	public static byte[] intToBytes(int value) {
		byte[] array = new byte[2];
		
		array[0] = (byte) (value / 128);
		array[1] = (byte) (value % 128);
		
		return array;
		
	}
	
	public static byte[] unshift(byte[] array, int value) {
		byte[] a = new byte[array.length + 1];
		
		a[0] = (byte) value;
		
		System.arraycopy(array, 0, a, 1, array.length);
		
		return a;
		
	}
	
	public static byte[] subArray(byte[] array, int fromIndex, int toIndex) {
		return Arrays.copyOfRange(array, fromIndex, toIndex);
	}
	
	public static int indexOf(byte[] array, int value) {
		int index = -1;
		
		for(int i = 0; i < array.length; i++) {
			if(index != -1) break;
			
			if(array[i] == (byte) value) index = i;
		}
		
		return index;
	}
	
	public static String toString(byte[] array) {
		String string = "";
		
		for(int i = 0; i< array.length; i++) {
			string += (char) array[i];
		}
		
		return string;
	}
}