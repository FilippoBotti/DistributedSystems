package it.unipr.botti;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

/**
 * Client is in charge of saving a activity object with its properties.
 * 
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

  public static void main(final String [] args) throws Exception {
    Random random = new Random();
    Registry registry   = LocateRegistry.getRegistry();
    Subscribe service   = (Subscribe) registry.lookup("subscribe");
    PriceWriter w = new PriceWriterImpl();

    service.subscribe(w);

    while (items < MAX_ITEMS)
    {
      //se server ha comunicato prezzo
      if(w.isBufferReady()){
        w.setBufferReaded();
        //prendo prezzo comunicato dal server e ne genero uno randomico nel client
        int serverPrice = w.getServerPrice();
        clientPrice = random.nextInt(MAX - MIN) + MIN;
        System.out.println("Prezzo ricevuto dal server: " + serverPrice);
        System.out.println("Prezzo client generato: " + clientPrice);
        //se il prezzo randomico del client >= quello del server
        if(clientPrice >= serverPrice){
          try
          {
            //invio il prezzo e nello stesso metodo ricevo la risposta del server, aumento gli item acquistati a seconda della risposta
            System.out.println("Invio: " + clientPrice);
            int response = service.sendPriceToServer(clientPrice);
            items += response;
            if(response==1)
              System.out.println("Venduto, ho un totale di " + items + " elementi");
            else
              System.out.println("Non venduto, rimango con " + items + " elementi");
          }
          catch (Exception e)
          {
            continue;
          }
        }
      }
      //attendo un secondo
      Thread.sleep(1000);
    }
    //esco dopo aver raggiunto 10 elementi, cancellando iscrizione e oggetto remoto
    System.out.print("Ho raggiunto i 10 elementi, termino");
    service.unsubscribe(w);
    UnicastRemoteObject.unexportObject(w, true);
    System.exit(0);
  }
}
