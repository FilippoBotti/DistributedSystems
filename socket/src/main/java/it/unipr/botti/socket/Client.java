package it.unipr.botti.socket;

import java.io.*;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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
  final static private String SHOST = "localhost";
  private static int items = 0;
  private final static int MAX_ITEMS = 5;


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
          // ricevo messaggio dal server
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
              System.out.println("Ricevuto prezzo: " + (int) serverMessage.getNewObject());
              int clientPrice = random.nextInt(MAX - MIN) + MIN;
              Message clientResponseMessage = new Message();
              if(clientPrice >= (int) serverMessage.getNewObject()){
                clientResponseMessage.setNewObject(clientPrice);
                clientResponseMessage.setRequestType(MessageType.CLIENT_PRICE);
              }
              else {
                clientResponseMessage.setNewObject(0);
                clientResponseMessage.setRequestType(MessageType.CLIENT_PASS);
              }
              System.out.println("Genero prezzo: " + clientPrice);
              outputStream.writeObject(clientResponseMessage);
              outputStream.flush();
              break;
            
            case SERVER_RESPONSE:
              boolean response = (boolean) serverMessage.getNewObject();
              if(response){
                items++;
              }
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
