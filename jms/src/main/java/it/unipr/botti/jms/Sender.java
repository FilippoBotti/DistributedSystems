package it.unipr.botti.jms;

import java.util.ArrayList;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
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
  private static final int NODE_NUMBER = 4;
  private int nodeId;
  ArrayList<QueueSender> queueSenders = new ArrayList<QueueSender>(NODE_NUMBER);
  private static QueueSession session;
  private static ActiveMQConnection connection;

  public Sender(final int nodeId){
    this.nodeId = nodeId;
  }

  public int getNodeIde(){
    return this.nodeId;
  }
  
  /*
   * This method sends a message to all of the node with id greather than the current id node.
   */
  public void sendElectionMessage(){
    try{
      CustomMessage mex = new CustomMessage(this.nodeId, "Election", MessageType.ELECTION);
      ObjectMessage message = session.createObjectMessage(mex);
      for(int i=nodeId; i<NODE_NUMBER-1; i++){
        queueSenders.get(i).send(message);
      }
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }

  public void sendMessageToAll(){
    try{
      CustomMessage mex = new CustomMessage(this.nodeId, "Coordinator", MessageType.NEW_COORDINATOR);
      ObjectMessage message = session.createObjectMessage(mex);
      for (QueueSender queueSender : queueSenders){
        queueSender.send(message);;
      }
    }
    catch (Exception e){
      e.printStackTrace();
    }
    
  }

  public void sendAcknowledgment(int id){
    try{
      System.out.println("Invio acknowledgment a: " + id);
      CustomMessage mex = new CustomMessage(this.getNodeIde(), "Acknoledgment", MessageType.ACKNOWLEDGMENT);
      ObjectMessage message = session.createObjectMessage(mex);
      queueSenders.get(id).send(message);
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }
  /**
   * Sends a sequence of messages.
   *
   * @param n  the number of messages.
   *
  **/
  public void createSenderQueue()
  {
    System.setProperty(
                "org.apache.activemq.SERIALIZABLE_PACKAGES",
                "java.util,org.apache.activemq,it.unipr.botti.jms");
    System.setProperty(
"org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
        
    connection = null;
    try
    {
      ActiveMQConnectionFactory cf =
        new ActiveMQConnectionFactory(Sender.BROKER_URL);

      connection = (ActiveMQConnection) cf.createConnection();

      connection.start();

      session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      for (int i=0; i<NODE_NUMBER; i++){
        if(i!=nodeId){
          Queue queue         = session.createQueue(Sender.QUEUE_NAME + "/" + i);
          QueueSender sender  = session.createSender(queue);
          queueSenders.add(sender);
        }
      }
      System.out.print("Succesfully created senders queue of size: " + queueSenders.size() + "\n");
    }
    catch (JMSException e)
    {
      e.printStackTrace();
    }
  }

  public void closeSenderQueue(){
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
