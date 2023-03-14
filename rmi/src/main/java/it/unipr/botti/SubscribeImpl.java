package it.unipr.botti;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

/**
 * SubscribeImpl (RMI) is the class implementation of Subscribe's interface.
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
   * A boolean is returned as aknoledgment: if it's true it means that the server has sold one item to the client.
   * @param int price
   * @return response
   * @throws RemoteException
   */

  @Override
  public boolean sendPriceToServer(int price) throws RemoteException 
  {
    System.out.println("Received price from client: " + price);
    boolean isSold = false;
    if(price>=serverSellingPrice){
      System.out.println("It can be sold: " + price + " >= " + serverSellingPrice);
      isSold = true;
    }
    else {
      System.out.println("It cannot be sold: " + price + " < " + serverSellingPrice);
    }
    return isSold;
  }

  /**
   * This method sets the price from the server.
   * It is used by the server only.
   * @param int price
   * @throws RemoteException
   */

  @Override
  public void setSellingPriceFromServer(int price) throws RemoteException
  {
    System.out.println("Current server selling price: " + price);
    serverSellingPrice = price;
  }
  
}
