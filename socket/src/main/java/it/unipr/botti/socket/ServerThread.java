package it.unipr.botti.socket;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * ServerThread provides a multithread connection
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * @author      Simone Montali <simone.montali1@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class ServerThread implements Runnable {
    private static int MAX = 200;
    private static int MIN = 10;
    private static final long SLEEPTIME = 200;
    private static int serverPrice;
    private static int clientPrice;
    private static boolean hasFinished = false;
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
     * For ever new client it creates a new thread
     * It provides every kind of request.
     * The first thing is the LOGIN and it is necessary for all the successive requests
     * Then we have: GET_EMPLOYEE (standard request or searched by headquarter or job),
     * ADD, EDIT and DELETE EMPLOYEE
     * and finally ADD, GET, EDIT and DELETE HEADQUARTER
     * For every request it checks exceptions and verifies the requirements.
     * If all is good, it sends a message and does the requests.
     */
    @Override
    public void run() {
        System.out.print("New connection accepted");
        Random random = new Random();
        try{
            this.outputStream = new ObjectOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));


            while(true){
                
                int generatedServerPrice = random.nextInt(MAX - MIN) + MIN;
                Message serverPrice = new Message(MessageType.SERVER_PRICE, generatedServerPrice);
                this.outputStream.writeObject(serverPrice);
                this.outputStream.flush();
                System.out.println("Numero inviato: " + serverPrice.getNewObject());
                if(this.inputStream == null)
                {
                    this.inputStream = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
                }
    
                Object receivedClientPrice = null;
                receivedClientPrice = this.inputStream.readObject();
    
    
                if(receivedClientPrice != null && receivedClientPrice instanceof Message)
                {
                    System.out.println("Prezzo ricevuto: " + (int)((Message)receivedClientPrice).getNewObject());
                    switch (((Message)receivedClientPrice).getRequestType()){
                        case CLIENT_PRICE:
                            int clientPrice = (int)((Message)receivedClientPrice).getNewObject();
                            Message serverResponse = new Message();
                            if(clientPrice>=generatedServerPrice)
                            {
                                serverResponse.setRequestType(MessageType.SERVER_RESPONSE);
                                serverResponse.setNewObject(true);
                            }
                            else
                            {
                                serverResponse.setRequestType(MessageType.SERVER_RESPONSE);
                                serverResponse.setNewObject(false);
                            }
                            outputStream.writeObject(serverResponse);
                            outputStream.flush();
                            break;

                        case CLIENT_FULL_ITEMS:
                            System.out.println("Ricevuta close connection");
                            if (this.server.getPool().getActiveCount() == 1)
                            {
                                
                                this.server.close();
                            }
                            this.socket.close();
                            return;

                        default:
                            break;
                    }
 
                }
                Thread.sleep(1000);
    
            }
            
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }

}