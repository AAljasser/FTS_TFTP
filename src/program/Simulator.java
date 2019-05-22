package program;
import java.io.*;
import java.net.*;
import java.util.*;

public class Simulator{
   
   private DatagramSocket listeningSocket;
   private DatagramPacket packet;
   
   public Simulator() throws Exception
   {
      this.listeningSocket = new DatagramSocket(29);
   }

   public void listen()
   {
        System.out.println("Simulator is Listening on Port 29: Waiting for packet.");
         while(true)
         {
		//listening to client queries
		byte[] data = new byte[512];
		this.packet = new DatagramPacket(data, data.length);

		try {
			this.listeningSocket.receive(this.packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//create a thread that will handle the request
		(new RequestHandler(this.packet)).start();
         }
   }

   private class RequestHandler extends Thread
   {
        private DatagramSocket clientSocket;
        private DatagramSocket serverSocket;

        private DatagramPacket toServerPacket;
        private DatagramPacket toClientPacket;
        private DatagramPacket fromClientPacket;
        private DatagramPacket fromServerPacket;

        InetSocketAddress client;
        InetSocketAddress server = new InetSocketAddress("localhost",69);

        RequestHandler(DatagramPacket clientPacket)
        {
            //construct the packet to send to the server
            this.toServerPacket = new DatagramPacket(clientPacket.getData(),clientPacket.getData().length, this.server);

            //create a client profile
            this.client = new InetSocketAddress(clientPacket.getAddress(), clientPacket.getPort());
           
            //create sockets
            try {
				this.clientSocket = new DatagramSocket();
				this.serverSocket = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }

        public void run()
        {
           this.initializeTransaction();
        }
        
        private void initializeTransaction()
        {
            while(true) //infinite loop
			{
         //send the packet to the server
			try {
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
         //wait for response from the server
			try {
				getServerResponse();
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
         //construct a client packet
			this.toClientPacket = new DatagramPacket(this.fromServerPacket.getData(), this.fromServerPacket.getData().length, this.client);
         //send the response to the client
			try {
				sendToClient();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
         //wait for response from the client
			try {
				getClientResponse();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.toServerPacket = new DatagramPacket(this.fromClientPacket.getData(), this.fromClientPacket.getData().length, this.server) 	;
			}
        }

        private void sendToClient() throws Exception
        {
            System.out.println("Simulator: Transfering to Client:");
            System.out.println("To host: " + this.toClientPacket.getAddress());
            System.out.println("Destination host port: " + this.toClientPacket.getPort());
            int len = this.toClientPacket.getLength();
            System.out.println("Length: " + len);
            System.out.println("Containing: ");
            for (int j=0;j<len;j++) {
            System.out.println("byte " + j + " " + this.toClientPacket.getData()[j]);
            }
            
            this.clientSocket.send(this.toClientPacket);
        }


        private void sendToServer() throws Exception
        {
         System.out.println("Simulator: Transfering to Server:");
         System.out.println("To host: " + this.toServerPacket.getAddress());
         System.out.println("Destination host port: " + this.toServerPacket.getPort());
         int len = this.toServerPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Containing: ");
         for (int j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.toServerPacket.getData()[j]);
         }
         this.serverSocket.send(this.toServerPacket);
        }


        private void getServerResponse() throws Exception
        {
         this.serverSocket.receive(this.fromServerPacket);
         // Process the received datagram.
         System.out.println("Simulator: Received from Server:");
         System.out.println("From host: " + this.fromServerPacket.getAddress());
         int clientPort = this.fromServerPacket.getPort();
         System.out.println("Host port: " + clientPort);
         int len = this.fromServerPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Containing: " );
         // print the bytes
         for (int j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.fromServerPacket.getData()[j]);
         }

        }


        private void getClientResponse() throws Exception
        {
            this.clientSocket.receive(this.fromClientPacket);

         // Process the received datagram.
         System.out.println("Simulator: Received from Client:");
         System.out.println("From host: " + this.fromClientPacket.getAddress());
         int clientPort = this.fromClientPacket.getPort();
         System.out.println("Host port: " + clientPort);
         int len = this.fromClientPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Containing: " );
         // print the bytes
         for (int j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.fromClientPacket.getData()[j]);
         }

        }

   }

   public static void main(String args[]) {
	   Simulator s = null;
	  try {
		s = new Simulator();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  s.listen();
   }
   
   
}


