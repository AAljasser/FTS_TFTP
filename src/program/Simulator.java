//
import java.io.*;
import java.net.*;
import java.util.*;

public class Simulator extends Thread{
   
   private DatagramSocket listeningSocket;
   private DatagramPacket packet;
   
   public Simulator() throws Exception
   {
      this.listeningSocket = new DatagramSocket(29);
   }

   public static void main(String arg[] )
   {
       try
       {
           Simulator proxy = new Simulator();
           proxy.start();

       }catch(Exception e){ System.out.println(e);}

   }

   public void run(){

        this.listen();

   }


   private void listen()
   {
        try {
            BufferedReader optionReader = new BufferedReader(new InputStreamReader(System.in));
           while(true)
           {
            //show options
            System.out.println("\t----------Welcome to the Simulator-------------\n\t Please select an option below then press <enter>\n\t\t1 DELAY PACKET \n\t\t2 LOSS PACKET \n\t\t3 DUPLICATE PACKETS \n");
            //user selects an option
            String pick = optionReader.readLine();
            System.out.println("You entered: "+pick);
           //user select the starting packet number
           System.out.println("\n Please insert the starting packet # to effect then press <enter> \n");
            String startNumber = optionReader.readLine();
            System.out.println("You entered: "+startNumber);
           //user select the finishing packet
           System.out.println("\n Please insert the finishing packet # to effect then press <enter> \n");
           String endNumber = optionReader.readLine();
            System.out.println("You entered: "+endNumber);
           //ends
           System.out.println("\n Packet #"+startNumber+" to #"+endNumber+" will experience OPTION "+pick+"\n");

           System.out.println("->Simulator:Listening on Port 29 \n->Simulator: Waiting for packet.");

            //listening to client queries
            byte[] data = new byte[512];
            packet = new DatagramPacket(data, data.length);

            this.listeningSocket.receive(this.packet);


            //create a thread that will handle the request
            (new RequestHandler(this.packet,pick,Integer.parseInt(startNumber),Integer.parseInt(endNumber))).start();
            return;
           }
        } catch (Exception se) 
        {
            se.printStackTrace();
            System.exit(1);
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

        private String clientPacketEffect = "0";
        private int clientPacketEffectLowerRange = 0;
        private int clientPacketUpperRange = 0;

        private int clientPacketSentCounter =0;

        InetSocketAddress client;
        InetSocketAddress server = new InetSocketAddress("localhost",69);

        RequestHandler(DatagramPacket clientPacket) throws Exception
        {
            //construct the packet to send to the server
            this.toServerPacket = new DatagramPacket(clientPacket.getData(),clientPacket.getData().length, this.server);

            //create a client profile
            this.client = new InetSocketAddress("localhost", clientPacket.getPort());

            //store the packet
            this.fromClientPacket = clientPacket;
            this.fromClientPacket.setSocketAddress(this.client);
           
            //create sockets
            this.clientSocket = new DatagramSocket();
            this.serverSocket = new DatagramSocket();
        }

        RequestHandler(DatagramPacket clientPacket, String type, int startRange, int endRange) throws Exception
        {
            this(clientPacket);
            this.clientPacketEffect = type;
            this.clientPacketUpperRange = endRange;
            this.clientPacketEffectLowerRange = startRange;
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
                this.toServerPacket = new DatagramPacket(this.fromClientPacket.getData(), this.fromClientPacket.getData().length, this.server);
                }
            }catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        private void sendToClient() throws Exception
        {
            System.out.println("->Simulator: Transfering Packet #"+this.clientPacketSentCounter+" to Client:");
            System.out.println("\t To host: " + this.toClientPacket.getAddress());
            System.out.println("\t Destination host port: " + this.toClientPacket.getPort());
            int len = this.toClientPacket.getLength();
            System.out.println("\t Length: " + len);
            /* System.out.println("Containing: ");
            for (int j=0;j<len;j++) {
            System.out.println("byte " + j + " " + this.toClientPacket.getData()[j]);
            } */

            this.clientSocket.send(this.toClientPacket);
        }


        private void sendToServer() throws Exception
        {
         this.clientPacketSentCounter++;
         
         System.out.println("->Simulator: Packet #"+this.clientPacketSentCounter+" Transfering to Server:");
         System.out.println("\t To host: " + this.toServerPacket.getAddress());
         System.out.println("\t Destination host port: " + this.toServerPacket.getPort());
         int len = this.toServerPacket.getLength();
         System.out.println("\t Length: " + len);
          System.out.println("Containing: ");
         for (int j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.toServerPacket.getData()[j]);
         } 
         if(this.clientPacketEffectLowerRange <= this.clientPacketSentCounter && this.clientPacketUpperRange >= this.clientPacketSentCounter )
         {
            switch(this.clientPacketEffect)
            {
                case "1":
                    System.out.println("\t ****DELAYING SENDING PACKET #"+this.clientPacketSentCounter+" TO SERVER");
                    Thread.sleep(9000);
                    break;
                case "2":
                    System.out.println("\t ****LOSING PACKET #"+this.clientPacketSentCounter+" FROM CLIENT");
                    return;
                case "3":
                    System.out.println("\t ****SENDING A DUPLICATE OF PACKET #"+this.clientPacketSentCounter+" TO SERVER");
                    this.serverSocket.send(this.toServerPacket);
                    break;
            }
         }
         this.serverSocket.send(this.toServerPacket);
        }


        private void getServerResponse() throws Exception
        {
            System.out.println("->Simulator: Waiting For Server Response");
            byte[] buff = new byte[512];
            this.fromServerPacket = new DatagramPacket(buff, buff.length);
         this.serverSocket.receive(this.fromServerPacket);
         // Process the received datagram.
         System.out.println("->Simulator: Received from Server:");
         System.out.println("\t From host: " + this.fromServerPacket.getAddress());
         int clientPort = this.fromServerPacket.getPort();
         System.out.println("\t Host port: " + clientPort);
         int len = this.fromServerPacket.getLength();
         System.out.println("\t Length: " + len);
         System.out.println("Containing: " );
         // print the bytes
         for (int j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.fromServerPacket.getData()[j]);
         } 

        }


        private void getClientResponse() throws Exception
        {
            System.out.println("->Simulator: Waiting For CLIENT Response");
            this.clientSocket.receive(this.fromClientPacket);

         // Process the received datagram.
         System.out.println("->Simulator: Received from Client:");
         System.out.println("\t From host: " + this.fromClientPacket.getAddress());
         int clientPort = this.fromClientPacket.getPort();
         System.out.println("\t Host port: " + clientPort);
         int len = this.fromClientPacket.getLength();
         System.out.println("\t Length: " + len);
          System.out.println("Containing: " );
         // print the bytes
         for (int j=0;j<len;j++) 
         {
            System.out.println("byte " + j + " " + this.fromClientPacket.getData()[j]);
         }

        }

   }


}
