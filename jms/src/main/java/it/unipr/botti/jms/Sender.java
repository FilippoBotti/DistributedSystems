package it.unipr.botti.jms;

import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * Class providing an implementation of a sender.
 *
**/

public class Sender
{
  private static final String BROKER_URL = "tcp://localhost:61616";
  private static final String QUEUE_NAME = "queue";
  private static final int NODE_NUMBER = 3;

  ArrayList<QueueSender> queueSenders = new ArrayList<QueueSender>(NODE_NUMBER);

  /**
   * Sends a sequence of messages.
   *
   * @param n  the number of messages.
   *
  **/
  public void createSenderQueue(final int n)
  {
    ActiveMQConnection connection = null;

    try
    {
      ActiveMQConnectionFactory cf =
        new ActiveMQConnectionFactory(Sender.BROKER_URL);

      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      QueueSession session =
        connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      for (int i=0; i<NODE_NUMBER; i++){
        Queue queue         = session.createQueue(Sender.QUEUE_NAME + "/" + i);
        QueueSender sender  = session.createSender(queue);
        queueSenders.add(sender);
      }

      TextMessage message = session.createTextMessage();

      for (int i = 0; i < n; i++)
      {
        message.setText("This is message " + (i + 1));
        queueSenders.get(i).send(message);
      }

      queueSenders.get(0).send(session.createMessage());

    }
    catch (JMSException e)
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
   * Starts the sender.
   *
   * @param args
   *
   * It does not need arguments.
   *
  **/
  public static void main(final String[] args)
  {
    final int n = 3;
    


    new Sender().createSenderQueue(n);
  }
}
