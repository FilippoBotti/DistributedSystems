package it.unipr.botti.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * Class providing an implementation of a publisher.
 *
**/

public class Publisher
{
  private static final String BROKER_URL   = "tcp://localhost:61616";
  private static final String TOPIC_NAME   = "topic";

  /**
   * Publishes a sequence of messages.
   *
   * @param n  the number of messages.
   *
  **/
  public void publish(final int n)
  {
    ActiveMQConnection connection = null;

    try
    {
      ActiveMQConnectionFactory cf =
        new ActiveMQConnectionFactory(Publisher.BROKER_URL);

      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      TopicSession session =
        connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

      Topic topic = session.createTopic(TOPIC_NAME);

      TopicPublisher publisher = session.createPublisher(topic);

      TextMessage message = session.createTextMessage();

      for (int i = 0; i < n; i++)
      {
        message.setText("This is message " + (i + 1));
        publisher.publish(message);
      }

      publisher.publish(session.createMessage());
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
   * Starts the publisher.
   *
   * @param args
   *
   * It does not need arguments.
   *
  **/
  public static void main(final String[] args)
  {
    final int n = 3;

    new Publisher().publish(n);
  }
}
