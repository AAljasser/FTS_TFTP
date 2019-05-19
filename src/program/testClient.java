package program;
import utilities.*;
import java.io.*;
import java.net.*;
public class testClient {
	
	
	public static void main(String args[]) {
		System.out.println("WOWOW");
		byte[] r = new byte[2];
		r[0] = (byte) 0b00000000;
		r[1] = (byte) 0b00000010;
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
			p = new DatagramPacket(b,b.length,InetAddress.getByName("174.114.91.59"),60300);
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
		
		
		byte[] d = new byte[2];
		d[0] = b[2];
		d[1] = b[3];
		BlockNum l = new BlockNum(d);
		System.out.println((int)b[0] + ""+(int)b[1] + ""+l.getInt()+ "");
		
		b = new byte[4];
		b[0] = 0b00000001;
		b[1] = 0b00000001;
		b[2] = 0b00000001;
		b[3] = 0b00000001;
		p = new DatagramPacket(b,b.length,p.getAddress(),p.getPort());
		
		try {
			s.send(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
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
