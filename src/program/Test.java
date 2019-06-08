package program;

import java.net.DatagramPacket;

public class Test {
	
	
	public static void main(String args[]) {
		Boolean x = true;
		
		
		System.out.println(x);
		change(x);
		System.out.println(x.booleanValue());
	}
	
	
	public  static void change(Boolean x) {
		x.booleanValue();
	}
}