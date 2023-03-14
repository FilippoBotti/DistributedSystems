package it.unipr.botti.socket;

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
  private static final int MAX = 200;
  private static final int MIN = 10;
  private static final int MAX_ITEMS = 10;
  private static int clientPrice;
  private static int items = 0;

  /**
   * First of all the client subscribe to the RMI service.
   * Then, util 10 items is reached it see if the server has communicated a new price to the RMI priceWriter with the !isBufferEmpty.
   * Then, it sets the buffer to empty, it generates a new price, and compare it with the current server price that is read with the RMI priceWriter.
   * If the client price is greater or equal than the current server price, the client sends its price to the server and
   * wait for the acknoledgment for the sale. If the sale is done, it aument the number of items bought.
   * If the number of items reaches 10 units the client terminates.
   * 
   * @param args
   * @throws Exception
   */

  public static void main(final String [] args) throws Exception {
    Random random = new Random();

    }

}
