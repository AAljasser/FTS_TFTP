import java.io.*;
import java.net.*;
public class testClient {
	
	
	public static void main(String args[]) {
		byte[] r = new byte[2];
		r[0] = (byte) 0b00000000;
		r[1] = (byte) 0b00000001;
		DatagramSocket s = null;
		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		DatagramPacket p=null;
		byte[] b = prepateWRQPacket(r,"test.txt","CcCC");
		try {
			p = new DatagramPacket(b,b.length,InetAddress.getLocalHost(),69);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			s.send(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		b = new byte[4];
		p = new DatagramPacket(b,b.length);
		try {
			s.receive(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println((int)b[0] + ""+(int)b[1] + ""+(int)b[2] + ""+(int)b[3] + "");
		
	}
	
	public static byte[] prepateWRQPacket(byte[] request, String filename, String mode) {

		int auxLength = 0;
		byte[] separator = { 0 };
		int length = request.length + filename.getBytes().length + mode.getBytes().length
				+ (2 * separator.length);

		byte[][] arrays = { request, filename.getBytes(), separator, mode.getBytes(), separator };
		byte[] message = new byte[length];

		for (byte[] array : arrays) {
			System.arraycopy(array, 0, message, auxLength, array.length);
			auxLength += array.length;
		}

		return message;
	}
}
