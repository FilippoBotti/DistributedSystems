package it.unipr.botti;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

/**
 * SubscribeImpl is in charge of providing methods for communications between
 * server and clients. 
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class SubscribeImpl extends UnicastRemoteObject implements Subscribe
{
  private static final long serialVersionUID = 1L;
  private static int serverSellingPrice;

  private Set<PriceWriter> writers;

  /**
   * This constructor sets all the Subscribe's parameters
   * @param Set<PriceWriter> s
   * @throws RemoteException
   */
  public SubscribeImpl(final Set<PriceWriter> s) throws RemoteException
  {
    this.writers = s;
    serverSellingPrice = 0;
  }

  /**
   * This method adds a new PriceWriter to the list of subscribers.
   * @param PriceWriter w
   * @throws RemoteException
   */

  @Override
  public void subscribe(final PriceWriter w) throws RemoteException
  {
    this.writers.add(w);
    System.out.println("New client accepted, total number of client: " + getWritersLength());
  }

  /**
   * This method removes a PriceWriter from the list of subscribers.
   * @param PriceWriter w
   * @throws RemoteException
   */

  @Override
  public void unsubscribe(final PriceWriter w) throws RemoteException
  {
    this.writers.remove(w);
    System.out.println("Client removed, total number of client: " + getWritersLength());
  }
  
  /**
   * This method gets the list of subscribers.
   * @throws RemoteException
   */

  @Override
  public int getWritersLength() throws RemoteException
  {
    return this.writers.size();
  }

  /**
   * This method gets the price from the client and verifies if it is greater than the server's price.
   * If it's greater, the server sells one item to the client, else nothing is done.
   * An integer is returned as aknoledgment.
   * @param int price
   * @return response
   * @throws RemoteException
   */

  @Override
  public int sendPriceToServer(int price) throws RemoteException 
  {
    System.out.println("Ricevuto client: " + price);
    int response = 0;
    if(price>=serverSellingPrice){
      System.out.println("Si può vendere: " + price + " <= " + serverSellingPrice);
      response++;
    }
    else {
      System.out.println("Non si può vendere: " + price + " > " + serverSellingPrice);
    }
    return response;
  }

  /**
   * This method sets the price from the server.
   * @param int price
   * @throws RemoteException
   */

  @Override
  public void setSellingPriceFromServer(int price) throws RemoteException
  {
    System.out.println("Prezzo di vendita server: " + price);
    serverSellingPrice = price;
  }
  
}
