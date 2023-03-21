package it.unipr.botti.socket;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class Client {
  final static private int SPORT = 4242;
  private static int MAX = 200;
  private static int MIN = 10;
  private static int NO_PURCHASE = 0;
  final static private String SHOST = "localhost";
  private static int items = 0;
  private final static int MAX_ITEMS = 10;

  public Client() {};

  /**
   * This method estabilishes every case of communication between server and client
   * The client wait for a message from the server: 
   *      - SERVER_PRICE: The server sends a new price and there are two cases:
   *                          - The client generates a random price and if it's greather or equal than the server's price, it sends
   *                            the price to the server. In case the client's price is less than the server's price, the client sends an empty message to the
   *                            server to communicate that it can't acquire the item.
   *                          - The client has reached the maximum amount of items it sends to the server a message to close the communication.
   *      - SERVER_RESPONSE: In this case the client check if the purchase works fine and updates the current amount of item purchased. 
   */
  public void run() {
    Random random = new Random();
      ObjectInputStream inputStream = null;
      ObjectOutputStream outputStream = null;

      try (Socket client = new Socket(SHOST, SPORT)){
        System.out.println("Connected");
        if(outputStream == null){
          outputStream = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream()));
        }
        if(inputStream == null){
          inputStream = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
        }

        while(true){
          Message serverMessage = (Message) inputStream.readObject();

          if(items >=MAX_ITEMS){
            Message clientCloseConnection = new Message(MessageType.CLIENT_FULL_ITEMS,0);
            outputStream.writeObject(clientCloseConnection);
            outputStream.flush();
            client.close();
            System.exit(0);
          }

          switch (serverMessage.getRequestType()) {
            case SERVER_PRICE:
              System.out.println("Received price from server: " + (int) serverMessage.getMessageObject());
              int clientPrice = random.nextInt(MAX - MIN) + MIN;
              Message clientResponseMessage = new Message();

              if(clientPrice >= (int) serverMessage.getMessageObject()){
                System.out.println("Generated price: " + clientPrice + ". Sending price to the server.");
                clientResponseMessage.setMessageObject(clientPrice);
                clientResponseMessage.setRequestType(MessageType.CLIENT_PRICE);
              }
              else {
                System.out.println("Generated price: " + clientPrice + ". Price not sended to the server.");
                clientResponseMessage.setMessageObject(NO_PURCHASE);
                clientResponseMessage.setRequestType(MessageType.CLIENT_PASS);
              }
              
              outputStream.writeObject(clientResponseMessage);
              outputStream.flush();
              break;
            
            case SERVER_RESPONSE:
              boolean response = (boolean) serverMessage.getMessageObject();
              if(response){
                items++;
              }
              System.out.println("Response from server: " + response + ". New amout of items: " + items);

            default:
              break;
          }
        }
      } catch (Exception e) {
          System.out.println("Exception occured in client main: " + e.getStackTrace());
          e.printStackTrace();
      }
  
  }

  public static void main (final String[] args) throws IOException {
      new Client().run();
  }

}
