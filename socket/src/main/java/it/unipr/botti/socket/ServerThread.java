package it.unipr.botti.socket;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * ServerThread provides a multithread connection
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class ServerThread implements Runnable {
    private static int MAX = 200;
    private static int MIN = 10;
    private static final long SLEEPTIME = 1000;
    private Server server;
    private Socket socket;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    /**
     * This constructor sets the Server and the Socket
     * @param Server s
     * @param Socket sck
     */
    
    public ServerThread(final Server s, final Socket sck) {
        this.server = s;
        this.socket = sck;
    }

    /**
     * This method estabilishes every case of communication between server and client
     * First of all the serverThread generates a new random price and communicates it to the corrispective client connected.
     * Then it wait for the client response message: 
     *      - CLIENT_PRICE_MESSAGE: The server receives a price from the client and check if it's greather or equal than the generated server price (with
     *                               this implementation it's always satisfied, an idea is to use a separated thread to generate the price), then 
     *                              the server sends a response message to the client with the selling acknoledgment.
     *      - CLIENT_FULL_ITEMS: In this case the client ask for closing the communication with the server because it reaches the max amount of 
     *                           items purchased. So the server close the socket and check if the client was the last, in this case it also close the server.
     */
    @Override
    public void run() {
        System.out.print("New connection accepted");
        Random random = new Random();

        try{
            this.outputStream = new ObjectOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            while(!this.server.isReady()){
                System.out.println("Wait for minimum number of client");
                Thread.sleep(SLEEPTIME);
            }
            while(true){
                int generatedServerPrice = random.nextInt(MAX - MIN) + MIN;
                Message serverPrice = new Message(MessageType.SERVER_PRICE, generatedServerPrice);
                this.outputStream.writeObject(serverPrice);
                this.outputStream.flush();
                System.out.println("Price generated from server: " + serverPrice.getMessageObject());

                if(this.inputStream == null)
                {
                    this.inputStream = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
                }
    
                Object receivedClientPrice = null;
                receivedClientPrice = this.inputStream.readObject();
    
                if(receivedClientPrice != null && receivedClientPrice instanceof Message)
                {
                    switch (((Message)receivedClientPrice).getRequestType()){
                        case CLIENT_PRICE:
                            int clientPrice = (int)((Message)receivedClientPrice).getMessageObject();
                            Message serverResponse = new Message();
                            if(clientPrice>=generatedServerPrice)
                            {
                                System.out.println("Received price from client: " + clientPrice + " >= " + generatedServerPrice + ". Server can sell.");
                                serverResponse.setRequestType(MessageType.SERVER_RESPONSE);
                                serverResponse.setMessageObject(true);
                            }
                            else
                            {
                                System.out.println("Received price from client: " + clientPrice + " < " + generatedServerPrice + ". Server can't sell.");
                                serverResponse.setRequestType(MessageType.SERVER_RESPONSE);
                                serverResponse.setMessageObject(false);
                            }
                            outputStream.writeObject(serverResponse);
                            outputStream.flush();
                            break;

                        case CLIENT_FULL_ITEMS:
                            System.out.println("Received close connection from client.");
                            if (this.server.getPool().getActiveCount() == 1)
                            {
                                System.out.println("There are no clients left, turn down server.");
                                this.server.close();
                            }
                            this.socket.close();
                            return;
                        
                        case CLIENT_PASS:
                            break;

                        default:
                            break;
                    }
                }
                Thread.sleep(SLEEPTIME);
            }
            
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}