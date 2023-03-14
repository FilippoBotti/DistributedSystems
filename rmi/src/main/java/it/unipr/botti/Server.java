package it.unipr.botti;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
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
  private static final int MIN_CLIENTS = 3;
  private static int serverPrice;
  
  /**
   * First of all the server wait for at least 3 clients.
   * Then it generate the random price and it save it in the RMI service object (it will be used for the comparison with the clients price).
   * After this the server communicates the price to all of the clients with the RMI priceWriters.
   * If the subscribers is zero then the server can terminates.
   * @param args
   * @throws Exception
   */

  public static void main(final String[] args) throws Exception
  {
    Random random = new Random();
    Registry registry = LocateRegistry.createRegistry(PORT);

    Set<PriceWriter> writers = new CopyOnWriteArraySet<>();

    Subscribe service = new SubscribeImpl(writers);

    registry.rebind("subscribe", service);

    System.out.println("\nServer running on port: " + PORT);

    while(service.getWritersLength()<MIN_CLIENTS){
      Thread.sleep(1000);
      System.out.println("Waiting for at least 3 clients. Current number of clients: " + service.getWritersLength());
    }

    while (service.getWritersLength()!=0)
    {
      serverPrice = random.nextInt(MAX - MIN) + MIN;
      service.setSellingPriceFromServer(serverPrice);
      Thread.sleep(2000);
      try
      {
        for (PriceWriter w : writers)
        {
          w.sendSellingPriceToClient(serverPrice);
        }
      }
      catch (Exception e)
      {
        continue;
      }
    }

    System.out.println("I served all clients. Goodbye!");
    UnicastRemoteObject.unexportObject(service, true);
    }
}
