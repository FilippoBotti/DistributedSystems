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
  private static QueueSession session;
  private static ActiveMQConnection connection;

  private int nodeId;
  private ArrayList<QueueSender> queueSenders;

  public Sender(final int nodeId){
    this.nodeId = nodeId;
    this.queueSenders = new ArrayList<QueueSender>(NODE_NUMBER);
  }

  public int getNodeIde(){
    return this.nodeId;
  }
  
  public ArrayList<QueueSender> getQueueSenders(){
    return this.queueSenders;
  }

  /*
   * This method creates a queueSender for all of the node in the network exept for the current node
   * All of the queue's name are in the format: "queue/node_identifier"
   */
  public void createSenderQueue()
  {
    System.setProperty(
                "org.apache.activemq.SERIALIZABLE_PACKAGES",
                "java.util,org.apache.activemq,it.unipr.botti.jms");
    System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
        
    connection = null;
    try
    {
      ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(Sender.BROKER_URL);
      connection = (ActiveMQConnection) cf.createConnection();
      connection.start();
      session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      for (int i=0; i<NODE_NUMBER; i++){
        Queue queue         = session.createQueue(Sender.QUEUE_NAME + "/" + i);
        QueueSender sender  = session.createSender(queue);
        queueSenders.add(sender);
      }

      System.out.print("Successfully created senders queue of size: " + queueSenders.size() + "\n");
      for (int i=0; i<queueSenders.size(); i++){
        System.out.println(queueSenders.get(i).getQueue().getQueueName());
      }
    }
    catch (JMSException e)
    {
      e.printStackTrace();
    }
  }

  /*
   * This method sends a message to all of the node with id greather than the current id node.
   */
  public void sendElectionMessage(){
    try{
      CustomMessage mex = new CustomMessage(this.nodeId, "Election", MessageType.ELECTION);
      ObjectMessage message = session.createObjectMessage(mex);
      for(int i=nodeId+1; i<queueSenders.size(); i++){
        //System.out.println("Sending election message to: " + queueSenders.get(i).getQueue().getQueueName());
        queueSenders.get(i).send(message);
      }
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }

  public void sendResourcesMessage(int coordinatorID, MessageType messageType){
    try{
      CustomMessage mex = new CustomMessage(this.nodeId, "Resources", messageType);
      ObjectMessage message = session.createObjectMessage(mex);
      System.out.println(queueSenders.get(coordinatorID).getQueue().getQueueName()) ;

      queueSenders.get(coordinatorID).send(message);
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }


  public void sendMessageToAll(){
    try{
      CustomMessage mex = new CustomMessage(this.nodeId, "Coordinator", MessageType.NEW_COORDINATOR);
      ObjectMessage message = session.createObjectMessage(mex);
      for(int i=0; i<queueSenders.size(); i++){
        if(i!=this.getNodeIde()){
          queueSenders.get(i).send(message);
        }
      }
    }
    catch (Exception e){
      e.printStackTrace();
    }
    
  }

  public void sendAcknowledgment(int id, MessageType ackMessageType){
    try{
      System.out.println("Invio " + ackMessageType + " a: " + id);
      CustomMessage mex = new CustomMessage(this.getNodeIde(), "Acknoledgment", ackMessageType);
      ObjectMessage message = session.createObjectMessage(mex);
      queueSenders.get(id).send(message);
    }
    catch (Exception e){
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
