package it.unipr.botti.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
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
 * Class providing an implementation of a server that accepts a request.
 *
**/

public class Server
{
  private static final String BROKER_URL   = "tcp://localhost:61616";
  private static final String BROKER_PROPS = "persistent=false&useJmx=false";
  private static final String QUEUE_NAME   = "server";

  /**
   * Receive a request and sends a reply.
   *
  **/
  public void receive()
  {
    ActiveMQConnection connection = null;
    try
    {
      BrokerService broker = BrokerFactory.createBroker(
          "broker:(" + BROKER_URL + ")?" + BROKER_PROPS);

      broker.start();

      ActiveMQConnectionFactory cf =
        new ActiveMQConnectionFactory(Server.BROKER_URL);
      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      QueueSession session =
        connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      Queue queue = session.createQueue(Server.QUEUE_NAME);

      QueueReceiver receiver = session.createReceiver(queue);

      Message request = receiver.receive();

      System.out.println("Message: " + ((TextMessage) request).getText());

      MessageProducer producer = session.createProducer(null);

      TextMessage reply = session.createTextMessage();

      reply.setText("Server reply message");
      reply.setJMSCorrelationID(request.getJMSCorrelationID());

      producer.send(request.getJMSReplyTo(), reply);
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
   * Starts the server.
   *
   * @param args
   *
   * It does not need arguments.
   *
  **/
  public static void main(final String[] args)
  {
    new Server().receive();
  }
}
