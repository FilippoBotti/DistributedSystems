package it.unipr.botti.jms;

import java.util.Random;

import javax.jms.JMSException;

public class Node {

    private static final int EXECUTION_TIME = 10000;
    private static final long PING_RATE_TIME = 1000;
    private static final long PING_CHECK_RATE_TIME = 6000;
    private static final long DEFAULT_WAIT_TIME = 5000;

    private int id;
    private Receiver receiver;
    private Sender sender;
    private State state;
    private int coordinatorId;
    private ResourceState resource;
    private int nodeWithResource;

    private Random random = new Random();
    private static final int MAX = 100;
	private static final int MIN = 0;
	private static final int H = 20;
	private static final int K = 1;
    private long prevMillisPing;
    private long initResoruceTime;
    private long lastPingReceived;

    public Node(final int id, final State state){
        this.id = id;
        this.receiver = new Receiver(id);
        this.sender = new Sender(id);
        this.state = state;
        this.coordinatorId = -1;
        this.resource = ResourceState.FREE;
        this.nodeWithResource = -1;
        this.prevMillisPing = System.currentTimeMillis();
    }

    public void setState(State state){
        this.state = state;
    }

    public State getState(){
        return this.state;
    }

    public void setResource(ResourceState value){
        this.resource = value;
    }

    public ResourceState getResourceState(){
        return this.resource;
    }


    public int getId(){
        return this.id;
    }

    public void consumeResource(){
        if(System.currentTimeMillis() - this.prevMillisPing >= PING_RATE_TIME){
            sender.sendResourcesMessage(this.coordinatorId, MessageType.PING_RESOURCE);
        }
        if(System.currentTimeMillis() -this.initResoruceTime >=EXECUTION_TIME){
            this.setState(State.EXECUTOR_IDLE);
            sender.sendResourcesMessage(this.coordinatorId, MessageType.FREE_RESOURCE);
            System.out.println("Resource consumed. My state is: " + this.getState());
        }
    }

    public void checkResourceState(){
        if(System.currentTimeMillis()-this.lastPingReceived >=PING_CHECK_RATE_TIME){
            System.out.println("Executor off, resource will be consider free from now");
            this.nodeWithResource=-1;
            this.resource= ResourceState.FREE;
        }
    }

    public void changeState() throws JMSException {
        int randomInt = this.random.nextInt(MAX - MIN) + MIN;

        if (randomInt < K+H && this.state!=State.DEAD && this.getState()!=State.COORDINATOR)
        {                
            System.out.println("I'm dead");
            this.setState(State.DEAD);
            this.resource = ResourceState.FREE; 
            this.coordinatorId=-1;
            this.nodeWithResource = -1;
            sleeping();
        }
        else if(randomInt >MAX -(K+H) && this.getState()==State.DEAD){
                this.receiver.flushQueue();
                this.setState(State.CANDIDATE);
                this.resource = ResourceState.FREE;
                this.coordinatorId=-1;
                this.nodeWithResource = -1;
                System.out.println("I'm back");
                election();     
            }
    }
    public void askForResource() {
        int randomInt = random.nextInt(MAX - MIN) + MIN;

        // 50% propability
        if (randomInt < 50)
        {
            sender.sendResourcesMessage(this.coordinatorId, MessageType.ASK_FOR_RESOURCES);
            this.setState(State.WAITING_FOR_RESOURCE);
            System.out.println("I'm asking for the resource. My state is " + this.getState());
        }
        
    }

    public void sleeping(){
        try{
            Thread.sleep(50000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void election(){
        try{
            //booleano: se ricevo almeno un acknowledgment aspetto la comunicazione del coordinatore
            CustomMessage receivedMessage;
            System.out.println("Election phase");
            if(this.getState()==State.CANDIDATE){
                sender.sendElectionMessage();
            }                
            receivedMessage = receiver.receiveElectionMessage(5000);
            while(this.coordinatorId==-1){
                switch (receivedMessage.getMessageType()) {
                    case ELECTION:
                        this.setState(State.CANDIDATE);
                        sender.sendAcknowledgment(receivedMessage.getSenderId(), MessageType.ELECTION_ACKNOWLEDGMENT);
                        sender.sendElectionMessage();
                        receivedMessage = receiver.receiveElectionMessage(5000);
                        break;
                    case ELECTION_ACKNOWLEDGMENT:
                        System.out.println("Ack received, waiting for coordinator");
                        this.setState(State.WAITING_FOR_COORDINATOR);
                        receivedMessage = receiver.receiveNewCordinatorMessage(10000);
                        break;
                    case NEW_COORDINATOR:
                        if (this.state != State.WAITING_FOR_COORDINATOR){
                            sender.sendElectionMessage();
                            receivedMessage = receiver.receiveElectionMessage(5000);
                        }
                        else {
                            System.out.println("Received new coordinator");
                            this.coordinatorId = receivedMessage.getSenderId();
                            this.setState(State.EXECUTOR_IDLE);
                        }
                        break;
                    default:
                    // ELECTION_TIME_OUT
                        if(this.state == State.WAITING_FOR_COORDINATOR){
                            receivedMessage = receiver.receiveElectionMessage(5000);
                        }
                        else {
                            System.out.println("No ack received, i'm the coordinator");
                            this.setState(State.COORDINATOR);
                            this.coordinatorId = this.id;
                            this.setResource(ResourceState.FREE);
                            sender.sendMessageToAll();
                        }
                        break;
                }
            }
            System.out.println("Election phase is done. My state is: " + this.state);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void executionPhase() throws JMSException{
        while(true){
            if(this.getState()==State.EXECUTOR_WITH_RESOURCE){
                consumeResource();
                this.prevMillisPing = System.currentTimeMillis();
            }
            if(this.getState()==State.COORDINATOR && this.getResourceState()==ResourceState.BUSY){
                checkResourceState();
            }
            //changeState();
            if(this.getState()!= State.COORDINATOR && this.getState()!=State.DEAD && this.getState()!=State.EXECUTOR_WITH_RESOURCE){
                askForResource();
            }
            if(this.getState()!=State.DEAD){
                CustomMessage receivedMessage = receiver.receiveResourcesMessage(5000);
                switch (receivedMessage.getMessageType()){
                    case ASK_FOR_RESOURCES:
                        if(this.state==State.COORDINATOR){
                            System.out.println("Resource has been requested by: " + receivedMessage.getSenderId() + ". Current resource's state is: " + this.getResourceState());
                            if (this.getResourceState()==ResourceState.FREE){
                                this.setResource(ResourceState.BUSY);
                                sender.sendAcknowledgment(receivedMessage.getSenderId(), MessageType.RESOURCES_ACKNOWLEDGMENT);
                                System.out.println("Resource has been assigned to: " + receivedMessage.getSenderId());
                                this.nodeWithResource = receivedMessage.getSenderId();
                                this.lastPingReceived = System.currentTimeMillis();
                            }
                            else{
                                sender.sendAcknowledgment(receivedMessage.getSenderId(), MessageType.RESOURCES_BUSY);
                                System.out.println("Resource has been requested by: " + receivedMessage.getSenderId() + " but it's busy");
                            }
                        }
                        break;
                    case NEW_COORDINATOR:
                        if (this.id > receivedMessage.getSenderId()){
                            sender.sendElectionMessage();
                            receivedMessage = receiver.receiveElectionMessage(5000);
                        }
                        else {
                            System.out.println("Received new coordinator");
                            this.coordinatorId = receivedMessage.getSenderId();
                            this.setState(State.EXECUTOR_IDLE);
                            System.out.println("Election phase is done. My state is: " + this.state);
                        }
                        break;
                    case TIMEOUT_ELECTION:
                        if(this.state == State.WAITING_FOR_COORDINATOR){
                            receivedMessage = receiver.receiveElectionMessage(5000);
                        }
                        else {
                            System.out.println("No ack received, i'm the coordinator");
                            this.setState(State.COORDINATOR);
                            this.coordinatorId = this.id;
                            this.setResource(ResourceState.FREE);
                            sender.sendMessageToAll();
                        }
                        break;
                    case TIMEOUT_RESOURCES:
                        if(this.state==State.WAITING_FOR_RESOURCE){
                            System.out.println("Timeout: coordinator is off, new election needed");
                            this.coordinatorId = -1;
                            this.setState(State.CANDIDATE);
                            election();
                        }
                        break;
                    case ELECTION:
                        this.coordinatorId = -1;
                        this.nodeWithResource = -1;
                        this.setState(State.CANDIDATE);
                        sender.sendAcknowledgment(receivedMessage.getSenderId(), MessageType.ELECTION_ACKNOWLEDGMENT);
                        election();
                        break;
                    case FREE_RESOURCE:
                        if(this.getState()==State.COORDINATOR){
                            System.out.println("Resource has been consumed by: " + receivedMessage.getSenderId());
                            this.setResource(ResourceState.FREE);
                            this.nodeWithResource = -1;
                        }
                        break;
                    case RESOURCES_ACKNOWLEDGMENT:
                        this.setState(State.EXECUTOR_WITH_RESOURCE);
                        System.out.println("Resource has been acquired. My state is: " + this.getState());
                        this.initResoruceTime = System.currentTimeMillis();
                        break;
                    case RESOURCES_BUSY:
                        this.setState(State.EXECUTOR_IDLE);
                        System.out.println("Resource has not been acquired. My state is: " + this.getState());
                        break;
                    case PING_RESOURCE:
                        System.out.println("Ping, ok");
                        this.lastPingReceived = System.currentTimeMillis();
                        break;
                    default:
                        // ELECTION_TIME_OUT
                        // System.out.println("No ack received, i'm the coordinator");
                        // this.setState(State.COORDINATOR);
                        // this.coordinatorId = this.id;
                        // sender.sendMessageToAll();
                        break;
                }
            }
        }
            
    }

    public static void main (final String[] args) {
       
        int id = Integer.parseInt(args[0]);
        State state;
        if(id==0){
            state = State.CANDIDATE;
        }
        else {
            state = State.EXECUTOR_IDLE;
        }
        Node n = new Node(id,state);

        System.out.println("Initializing node ...");
        n.sender.createSenderQueue();
        n.receiver.createReceiverQueue();
        System.out.println("\n\n");
        try{
            Thread.sleep(4000);
        } catch (Exception e){
            e.printStackTrace();
        }
        n.election();
        try {
            n.executionPhase();
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
    }
}
