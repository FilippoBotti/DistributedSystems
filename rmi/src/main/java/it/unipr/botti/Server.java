package it.unipr.botti;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Client is in charge of saving a activity object with its properties.
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class Server
{
  private static final int PORT = 1099;
  private static final int MAX = 200;
  private static final int MIN = 10;
  private static final int MIN_CLIENTS = 10;
  private static int serverPrice;
  private static boolean isClientsReady = false;
  
  public static void main(final String[] args) throws Exception
  {
    Random random = new Random();
    Registry registry = LocateRegistry.createRegistry(PORT);

    Set<PriceWriter> writers = new CopyOnWriteArraySet<>();

    Subscribe service = new SubscribeImpl(writers);

    registry.rebind("subscribe", service);

    System.out.println("\nServer running on port: " + PORT);

    //attendo il numero necessario di client
    while(!isClientsReady){
      Thread.sleep(1000);
      System.out.println("Attendo il numero di client minimo (3) per partire, client connessi: " + service.getWritersLength());
      if(service.getWritersLength()>=MIN_CLIENTS){
        isClientsReady = true;
      }
    }

    //se ho raggiunto il numero necessario di client
    while (true)
      {
        //genero prezzo random, lo comunico al subscribe perchè mi servirà successivamente per i confronti coi prezzi ricevuti dai client
        serverPrice = random.nextInt(MAX - MIN) + MIN;
        service.setSellingPriceFromServer(serverPrice);
        Thread.sleep(2000);
        try
        {
          //comunico il prezzo ai client tramite il writer corrispondente
          for (PriceWriter w : writers)
          {
            w.sendSellingPrice(serverPrice);
          }
        }
        catch (Exception e)
        {
          continue;
        }
        //verifico se ho servito ogni client
        if(service.getWritersLength()==0){
          System.out.println("Ho servito tutti i client, termino");
          UnicastRemoteObject.unexportObject(service, true);
          System.exit(0);
        }
      }
    }
}
