package it.unipr.botti.jms;


import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class Node {

    private int id;
    private Receiver receiver;
    private Sender sender;
    private State state;

    public Node(final int id){
        this.id = id;
        this.receiver = new Receiver(id);
        this.sender = new Sender(id);
        this.state = State.ATTIVO;
    }

    public void setState(State state){
        this.state = state;
    }

    public State getState(){
        return this.state;
    }


    public int getId(){
        return this.id;
    }

    public void election(){
        try{
            //booleano: se ricevo almeno un aknoledgment aspetto la comunicazione del coordinatore
            CustomMessage receivedMessage;
            boolean hasReceivedAck = false;
            while(true){
                receivedMessage = receiver.receiveElectionMessage(2000);
                if(receivedMessage.getMessageType()==MessageType.ELECTION){
                    //ricevuto messaggio di elezione, rispondo con ack e invio messaggio di elezione a tutti
                    System.out.println("Ricevuto mex da: " + receivedMessage.getSenderId());
                    sender.sendAcknowledgment(receivedMessage.getSenderId());
                    sender.sendElectionMessage();
                }
                else if (receivedMessage.getMessageType()==MessageType.ACKNOWLEDGMENT){
                    System.out.println("Ricevuto ack da: " + receivedMessage.getSenderId());
                    hasReceivedAck = true;
                }
                else if (receivedMessage.getMessageType()==MessageType.NEW_COORDINATOR && !hasReceivedAck) {
                    System.out.println("SONO IL COORDINATORE");
                }
            }
           
            
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main (final String[] args) {
       
        int id = Integer.parseInt(args[0]);
        Node n = new Node(id);

        n.sender.createSenderQueue();
        n.receiver.createReceiverQueue();
        System.out.println("\n\n\n\n\n\n");
        try{
            Thread.sleep(5000);
        } catch (Exception e){
            e.printStackTrace();
        }
        n.sender.sendElectionMessage();
        n.election();
       
    }
}
