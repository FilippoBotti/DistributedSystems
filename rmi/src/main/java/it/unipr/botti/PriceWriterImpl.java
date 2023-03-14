package it.unipr.botti;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * PriceWriterImpl (RMI) is the class implementation of PriceWriter's interface.
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class PriceWriterImpl extends UnicastRemoteObject implements PriceWriter {
  private static final long serialVersionUID = 1L;
  private static int serverPrice;
  private static boolean isBufferEmpty;


  /**
   * This constructor sets all the PriceWriter's parameters
   * 
   * @throws RemoteException
   */
    
  public PriceWriterImpl() throws RemoteException {
    isBufferEmpty = true;
    serverPrice = 0;
  }

  /**
   * This method gets the price which has been sets by the Server.
   * This method is used by the clients only.
   * @throws RemoteException
   */

  @Override
  public int getServerPrice() throws RemoteException
  {
    return serverPrice;
  }

  /**
   * This method sets the price and sets buffer to ready (which means that the server has generated a new price).
   * This method is used by the server only.
   * @param int price
   * @throws RemoteException
   */

  @Override
  public void sendSellingPriceToClient(final int price) throws RemoteException {
    serverPrice = price;
    isBufferEmpty = false;
  }

  /**
   * This method tells if the buffer is ready or empty.
   * This method is used by the clients only to see if the server has generated a new price.
   * @throws RemoteException
   */

  @Override
  public boolean isBufferEmpty() throws RemoteException 
  {
    return isBufferEmpty;
  }

  /**
   * This method sets the buffer to empty.
   * This method is used by the clients only to avoid the reception of the same server's price.
   * @throws RemoteException
   */
  
  @Override
  public void setBufferToEmpty() throws RemoteException 
  {
    isBufferEmpty = true;
  }

}
