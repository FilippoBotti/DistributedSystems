package it.unipr.botti;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Client is in charge of saving a activity object with its properties.
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class PriceWriterImpl extends UnicastRemoteObject implements PriceWriter {
  private static final long serialVersionUID = 1L;
  private static int serverPrice;
  private static boolean isBufferReady;


  /**
   * This constructor sets all the PriceWriter's parameters
   * 
   * @throws RemoteException
   */
    
  public PriceWriterImpl() throws RemoteException {
    isBufferReady = false;
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
   * @param int receivedServerPrice
   * @throws RemoteException
   */

  @Override
  public void sendSellingPrice(final int receivedServerPrice) throws RemoteException {
    serverPrice = receivedServerPrice;
    isBufferReady = true;
  }

  /**
   * This method tells if the butter is ready or empty.
   * This method is used by the clients only to see if the server has generated a new price.
   * @throws RemoteException
   */

  @Override
  public boolean isBufferReady() throws RemoteException 
  {
    return isBufferReady;
  }

  /**
   * This method sets the buffer to empty.
   * This method is used by the clients only to avoid the reception of the same server's price.
   * @throws RemoteException
   */
  
  @Override
  public void setBufferReaded() throws RemoteException 
  {
    isBufferReady = false;
  }

}
