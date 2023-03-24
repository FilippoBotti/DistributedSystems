package it.unipr.botti.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

/**
 *
 * Class providing an implementation of a receiver.
 *
**/

public class Receiver
{
  private static final String BROKER_URL   = "tcp://localhost:61616";
  private static final String BROKER_PROPS = "persistent=false&useJmx=false";
  private static int id;

  public Receiver(final int queueId){
    id = queueId;
  }
  /**
   * Receives a sequence of messages.
   *
  **/
  public void receive()
  {
    ActiveMQConnection connection = null;
    try
    {

      ActiveMQConnectionFactory cf =
        new ActiveMQConnectionFactory(Receiver.BROKER_URL);

      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      QueueSession session =
        connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      String queueName = "queue/" + id;
      Queue queue = session.createQueue(queueName);

      QueueReceiver receiver = session.createReceiver(queue);

      while (true)
      {
        Message message = receiver.receive();

        if (message instanceof TextMessage)
        {
          System.out.println("Message: " + ((TextMessage) message).getText());
        }
        else
        {
          break;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (connection != null)
      {
        try
        {
          connection.close();
        }
        catch (JMSException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Starts the receiver.
   *
   * @param args
   *
   * It does not need arguments.
   *
  **/
  public static void main(final String[] args)
  {
    new Receiver(3).receive();
  }
}
