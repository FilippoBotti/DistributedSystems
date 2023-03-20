package it.unipr.botti.socket;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class Server {

    private static final int SPORT = 4242;
    private static final int COREPOOL = 5;
    private static final int MAXPOOL = 100;
    private static final long IDLETIME = 5000;

    private ServerSocket socket;
    private ThreadPoolExecutor pool;

    /**
     * This constructor create a new socket
     * 
     * @throws IOException
     */
    
    public Server() throws IOException
    {
        this.socket = new ServerSocket(SPORT);
    }


    /**
     * This method provvide a connection between server and client
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        ArrayList<Socket> clientList = new ArrayList<Socket>();
        boolean isReady = false;
        this.pool = new ThreadPoolExecutor(COREPOOL, MAXPOOL, IDLETIME, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        System.out.println("I'm running on port"+SPORT);
        // this.socket.setSoTimeout(0);
        while(true)
        {
            try
            {
                    Socket s = this.socket.accept();
                    this.pool.execute(new ServerThread(this, s));
                    
            } catch (Exception e)
            {
                break;
            }
        } 
        System.out.println("Chiudo server");
        this.pool.shutdown();

    }

    /**
     * This method closes the socket
     */
    

 
    public void close()
    {
        try
        {
            this.socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ThreadPoolExecutor getPool()
    {
        return this.pool;
    }

    public static void main (final String[] args) throws IOException {
        new Server().run();
    }
}