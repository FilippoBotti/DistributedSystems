package it.unipr.botti;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Subscribe is the interface for the RMI object that provides
 * communications between server and clients.
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public interface Subscribe extends Remote {
  void subscribe(final PriceWriter w) throws RemoteException;
  void unsubscribe(final PriceWriter w) throws RemoteException;
  int getWritersLength() throws RemoteException;
  boolean sendPriceToServer(final int t) throws RemoteException;
  void setSellingPriceFromServer(final int t) throws RemoteException;
}
