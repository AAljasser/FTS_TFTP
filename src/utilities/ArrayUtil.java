package utilities;
import java.util.Arrays;

public class ArrayUtil {
	
	private ArrayUtil(){}
	
	//Method to break an array into subArrays
	//if addzero is true, it add an array of size zero at the end of the array when the  size of original is modulo of sublength
	//i.e it adds an array of size 0 when the file size is 508;
	public static byte[][] makeBidimensional(byte[] original, int subLength, boolean addZero) {

		int auxLength = (int) Math.ceil((double) original.length / subLength);
		
		int mainArrayLength = (addZero && original.length % subLength == 0) ? auxLength + 1 : auxLength;

		byte[][] mainArray = new byte[mainArrayLength][];

		int fromIndex = 0;
		int toIndex = 0;
		int remainder = original.length;

		for (int i = 0; i < mainArrayLength; i++) {

			if (remainder < subLength) {

				toIndex += remainder;

				mainArray[i] = Arrays.copyOfRange(original, fromIndex, toIndex);
			} else {			

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
		
		System.arraycopy(container, 0, array, 0, container.length);
		array[array.length - 1] = subArray;
		
		/*
		 * for(int i = 0; i < array.length; i++) { if(i == array.length - 1) array[i] =
		 * subArray;
		 * 
		 * else array[i] = container[i]; }
		 */
		
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