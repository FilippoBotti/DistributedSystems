package it.unipr.botti;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * PriceWriter is the interface for the RMI object that provides
 * communications between client and client.
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public interface PriceWriter extends Remote {
  void sendSellingPriceToClient(final int t) throws RemoteException;
  int getServerPrice() throws RemoteException;
  boolean isBufferEmpty() throws RemoteException;
  void setBufferToEmpty() throws RemoteException;
}