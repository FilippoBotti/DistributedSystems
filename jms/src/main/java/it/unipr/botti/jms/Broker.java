package it.unipr.botti.jms;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class Broker {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String BROKER_PROPS = "persistent=false&useJmx=false";
    public static void main (final String[] args) {
        BrokerService broker;
        try {
        broker = BrokerFactory.createBroker(
            "broker:(" + BROKER_URL + ")?" + BROKER_PROPS);
        broker.start(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    while(true){}

    }
}
