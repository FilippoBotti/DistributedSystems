package it.unipr.botti.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
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
  private int nodeId;
  private static ActiveMQConnection connection;
  private static QueueReceiver receiver;

  public Receiver(final int nodeId){
    this.nodeId = nodeId;
  }

  public void createReceiverQueue(){
    System.setProperty(
                "org.apache.activemq.SERIALIZABLE_PACKAGES",
                "java.util,org.apache.activemq,it.unipr.botti.jms");
                System.setProperty(
"org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
        
    connection = null;
    try
    {

      ActiveMQConnectionFactory cf =
        new ActiveMQConnectionFactory(Receiver.BROKER_URL);

      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      QueueSession session =
        connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      String queueName = "queue/" + nodeId;
      Queue queue = session.createQueue(queueName);

      receiver = session.createReceiver(queue);
      System.out.print("Succesfully created receiver queue " + receiver.getQueue().getQueueName() + "\n");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }
  /**
   * Receives a sequence of messages.
   *
  **/
  public CustomMessage receiveElectionMessage(long timeout)
  {
    try
    {
      Message message = receiver.receive(timeout);

      if (message instanceof ObjectMessage)
      {
        CustomMessage mex = (CustomMessage)((ObjectMessage) message).getObject();
        System.out.println(mex.toString());
        return mex;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return new CustomMessage(0, "Coordinatore", MessageType.NEW_COORDINATOR);
    
  }

  public MessageType receiveNewCordinatorMessage()
  {
    try
    {
      Message message = receiver.receive();
      System.out.println("ReceiviNG");
      if (message instanceof ObjectMessage)
      {
        CustomMessage mex = (CustomMessage)((ObjectMessage) message).getObject();
        System.out.println(mex.toString());
        return mex.getMessageType();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return MessageType.ERROR;
  }


public void closeReceiverQueue(){
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
  }
}
