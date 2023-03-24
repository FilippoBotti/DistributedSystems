package it.unipr.botti.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

/**
 *
 * Class providing an implementation of a subscriber.
 *
**/

public class Subscriber
{
  private static final String BROKER_URL   = "tcp://localhost:61616";
  private static final String BROKER_PROPS = "persistent=false&useJmx=false";
  private static final String TOPIC_NAME   = "topic";

  /**
   * Receives a sequence of messages.
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
        new ActiveMQConnectionFactory(Subscriber.BROKER_URL);

      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      TopicSession session =
        connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

      Topic topic = session.createTopic(TOPIC_NAME);

      TopicSubscriber subscriber = session.createSubscriber(topic);

      while (true)
      {
        Message message = subscriber.receive();

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
   * Starts the subscriber.
   *
   * @param args
   *
   * It does not need arguments.
   *
  **/
  public static void main(final String[] args)
  {
    new Subscriber().receive();
  }
}
