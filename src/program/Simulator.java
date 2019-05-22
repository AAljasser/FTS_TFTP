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

   public listen()
   {
        try {
            System.out.println("Simulator is Listening on Port 29: Waiting for packet.");
           while(true)
           {
            //listening to client queries
            data = new byte[512];
            packet = new DatagramPacket(data, data.length);

            this.listeningSocket.receive(this.packet);

            //create a thread that will handle the request
            (new RequestHandler(this.packet)).start();
           }
        } catch (SocketException se) 
        {
            se.printStackTrace();
            System.exit(1);
         }
   }

   private class RequestHandler extends Thread()
   {
        private DatagramSocket clientSocket;
        private DatagramSocket serverSocket;

        private DatagramPakcet toServerPacket;
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
            this.clientSocket = new DatagramSocket();
            this.serverSocket = new DatagramSocket();
        }

        public void run()
        {
           this.initializeTransaction();
        }
        
        private void initializeTransaction()
        {
            try{
                while(true) //infinite loop
                {
            //send the packet to the server
                sendToServer();
            //wait for response from the server
                getServerResponse();
            //construct a client packet
                this.toClientPacket = new DatagramPacket(this.fromServerPacket.getData(), this.fromServerPacket.getData().length, this.client);
            //send the response to the client
                sendToClient();
            //wait for response from the client
                getClientResponse();
                this.toServerPacket = new DatagramPacket(this.fromClientPacket.getData(), this.fromClientPacket.getData().length, this.server)
                }
            }catch(IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        private void sendToClient() throws Exception
        {
            System.out.println("Simulator: Transfering to Client:");
            System.out.println("To host: " + this.toClientPacket.getAddress());
            System.out.println("Destination host port: " + this.toClientPacket.getPort());
            len = this.toClientPacket.getLength();
            System.out.println("Length: " + len);
            System.out.println("Containing: ");
            for (j=0;j<len;j++) {
            System.out.println("byte " + j + " " + this.toClientPacket.getData()[j]);
            }
            
            this.clientSocket.send(this.toClientPacket);
        }


        private void sendToServer() throws Exception
        {
         System.out.println("Simulator: Transfering to Server:");
         System.out.println("To host: " + this.toServerPacket.getAddress());
         System.out.println("Destination host port: " + this.toServerPacket.getPort());
         len = this.toServerPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Containing: ");
         for (j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.toServerPacket.getData()[j]);
         }
         this.serverSocket.send(this.toServerPacket)
        }


        private void getServerResponse() throws Exception
        {
         this.serverSocket.receive(this.fromServerPacket);
         // Process the received datagram.
         System.out.println("Simulator: Received from Server:");
         System.out.println("From host: " + this.fromServerPacket.getAddress());
         clientPort = this.fromServerPacket.getPort();
         System.out.println("Host port: " + clientPort);
         len = this.fromServerPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Containing: " );
         // print the bytes
         for (j=0;j<len;j++) 
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
         clientPort = this.fromClientPacket.getPort();
         System.out.println("Host port: " + clientPort);
         len = this.fromClientPacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Containing: " );
         // print the bytes
         for (j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.fromClientPacket.getData()[j]);
         }

        }

   }


}

