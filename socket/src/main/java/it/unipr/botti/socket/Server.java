package it.unipr.botti.socket;

import java.util.Random;


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
  }
}
