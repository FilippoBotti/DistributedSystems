package it.unipr.botti.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * Class providing an implementation of a client that sends a request
 * and waits for an answer.
 *
**/

public class Client
{
  private static final String BROKER_URL = "tcp://localhost:61616";
  private static final String QUEUE_NAME = "server";

  /**
   * Sends a sequence of messages.
   *
   * @param n  the number of messages.
   *
  **/
  public void send(final int n)
  {
    ActiveMQConnection connection = null;

    try
    {
      ActiveMQConnectionFactory cf =
          new ActiveMQConnectionFactory(Client.BROKER_URL);

      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      QueueSession session =
        connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      Destination serverQueue  = session.createQueue(QUEUE_NAME);
      MessageProducer producer = session.createProducer(serverQueue);

      Destination tempDest = session.createTemporaryQueue();

      MessageConsumer consumer = session.createConsumer(tempDest);

      TextMessage request = session.createTextMessage();

      request.setText("Client request message");
      request.setJMSReplyTo(tempDest);
      request.setJMSCorrelationID("123");

      producer.send(request);

      Message reply = consumer.receive();

      System.out.println("Message: " + ((TextMessage) reply).getText());
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
   * Starts the client.
   *
   * @param args
   *
   * It does not need arguments.
   *
  **/
  public static void main(final String[] args)
  {
    final int n = 3;

    new Client().send(n);
  }
}
