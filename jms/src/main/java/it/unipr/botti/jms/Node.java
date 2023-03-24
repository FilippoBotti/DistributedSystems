package it.unipr.botti.jms;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class Node {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String BROKER_PROPS = "persistent=false&useJmx=false";
    private int id;
    private Receiver receiver;

    public Node(final int id){
        this.id = id;
        this.receiver = new Receiver(id);
    }

    public int getId(){
        return this.id;
    }

    public static void main (final String[] args) {
        int id = Integer.parseInt(args[0]);
        Node n = new Node(id);
        if(id == 0){
            BrokerService broker;
            try {
            broker = BrokerFactory.createBroker(
                "broker:(" + BROKER_URL + ")?" + BROKER_PROPS);
            broker.start();

            } catch (Exception e) {
            e.printStackTrace();
            }
        }
        while(true){

        }
    }
}
