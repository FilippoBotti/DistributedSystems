package it.unipr.botti.jms;

import java.util.Random;

import javax.jms.JMSException;

public class Node {

    private static final int DEFAULT_EXECUTION_TIME = 10000;
    private static final long PING_RATE_TIME = 1000;
    private static final long PING_CHECK_RATE_TIME = 6000;
    private static final long DEFAULT_WAIT_TIME = 1000;

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
	private static final int K = 5;
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

    /*
     * This method simulates the resource's utilization.
     * 
     * @param int delta
     * 
     * It accept one integer which will be used to determinate the execution time.
     * It is also in charge to send ping messages to the coordinator in order to prevent resource's revokation from the coordinator
     * After the execution time it will sleep the node for a random amount of time in range of 5 seconds.
     * 
     */
    public void consumeResource(int delta){
        if(System.currentTimeMillis() - this.prevMillisPing >= PING_RATE_TIME){
            sender.sendResourcesMessage(this.coordinatorId, MessageType.PING_RESOURCE);
        }
        if(System.currentTimeMillis() - this.initResoruceTime >= DEFAULT_EXECUTION_TIME*delta){
            this.setState(State.EXECUTOR_IDLE);
            sender.sendResourcesMessage(this.coordinatorId, MessageType.FREE_RESOURCE);
            System.out.println("\uD83C\uDD93 Resource consumed. My state is: " + this.getState());
            int randomWaitTime = random.nextInt(5) + 1;
            System.out.println("\uD83D\uDE34 I will sleep for " +randomWaitTime + " seconds");
            sleeping(randomWaitTime);
        }
    }

    /*
     * This method is in charge to verify if the node which has the resource is still awake.
     * It verifies if the last ping message has been received in the corrrect interval time
     * If not it will be consider the node dead and it will rewoke the resource
     */
    public void checkResourceState(){
        if(System.currentTimeMillis()-this.lastPingReceived >= PING_CHECK_RATE_TIME){
            System.out.println("Executor off, resource will be consider free from now");
            this.nodeWithResource=-1;
            this.resource= ResourceState.FREE;
        }
    }

    /*
     * This method provides the state changes.
     * If a node is awake and the random probability is less than K it will put the node in the dead state and 
     * it will sleep the node for 5 seconds.
     * Else if a node is dead and the random probability is less than H+K it will wake the node up (flush the queue messages) and it will start new election phase,
     * with the node in the candidate state
     */
    public void changeState() throws JMSException {
        int randomInt = this.random.nextInt(MAX - MIN) + MIN;

        if (randomInt < K && this.getState()!=State.DEAD)
        {                
            System.out.println("\uD83D\uDC80 I'm dead");
            this.setState(State.DEAD);
            this.resource = ResourceState.FREE; 
            this.coordinatorId=-1;
            this.nodeWithResource = -1;
            sleeping(5);
        }
        else if(randomInt < K + H && this.getState()==State.DEAD){
                this.receiver.flushQueue();
                this.setState(State.CANDIDATE);
                this.resource = ResourceState.FREE;
                this.coordinatorId=-1;
                this.nodeWithResource = -1;
                System.out.println("\uD83D\uDD19 I'm back");
                election();     
            }
        
    }

    /*
     * This method randomly chooses to ask the coordinator for the resource.
     * It puts the node in the waiting_for_resource state if the random probability is less than 50 and it will send an ask_for_resource message to the coordinator
     * Else it will sleep the node for a random amount of time (in range of 5 seconds)
     */
    public void askForResource() {
        int randomInt = random.nextInt(MAX - MIN) + MIN;
        if (randomInt < 50)
        {
            sender.sendResourcesMessage(this.coordinatorId, MessageType.ASK_FOR_RESOURCES);
            this.setState(State.WAITING_FOR_RESOURCE);
            System.out.println("I'm asking for the resource. My state is " + this.getState());
        }
        else {
            int randomWaitTime = random.nextInt(5) + 1;
            System.out.println("\uD83D\uDE34 I don't need the resource, I will sleep for " + randomWaitTime +  " seconds");
            sleeping(randomWaitTime);
        }
    }

    /*
     * This methods sleeps the node for delta seconds
     * 
     * @param int delta
     */
    public void sleeping(int delta){
        try{
            Thread.sleep(delta*DEFAULT_WAIT_TIME);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * This method provides bully's algorithm implementation for the election phase.
     * If the node is candidate it will send an election messages to all the nodes with id greater than the current node id.
     * Then, until the coordinator is not elected, it will wait for response messages:
     * If the message's type is election it will send an acknowledgment to the node which has send the message, and send a message to all of the 
     * nodes with id greater than the current node id.
     * If it's an acknowledgment it will wait for the coordinator.
     * If it's a new_coordinator_messages it will put the node in the executor state, but only if the node was waiting for the coordinator (cause if it's not
     * it means that the node didn't receive any acknowledgment, so the election is not correct and new election will be required)
     * If it's an election_timeout_message and the node was waiting for the coordinator it will wait for another message (it means that the election isn't done),
     * else it means that the node has not receive any acknowledgment so the node is the coordinator and it will send new_coordinator_message to all of the other nodes
     */

    public void election(){
        try{
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
            System.out.println("Election phase is done. My state is: " + this.state +"\n");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
     * This method provides the execution phase.
     * First if the node invoke tha change state methods to randomly change the state
     * Then if the node has the resource it will consume it with the respective method
     * If the node it the coordinator and the resource is assigned it will invoke the method to check the resource state with ping messages
     * If the node is an executor without the resource it will invoke the metode to randomly ask for the resource.
     * 
     * If the node is not dead it will wait for the message:
     * If the message is an ask for the resource and the node is the coordinator it will check the resource state and assign it if it's free
     * If the message is a new coordinator it means that a new election has been done by a node with a greather id so it simply change the state to executor
     * If the message is election it means that new election has been initiate and it will invoke the election phase (also for the timeout_election message)
     * If the message is timeout resource and the node was waiting for the resource it will invoke new election cause it means that the coordinator is dead
     * If the message is free resource it will revoke the resource from the node (it means that the node has consumed the resource)
     * If the message is resource acknownledgment it means that the node can get the resource, if it's resource_busy it means that the resource is already assigned to another node
     */
    public void executionPhase() throws JMSException{
        while(true){
            changeState();

            if(this.getState()==State.EXECUTOR_WITH_RESOURCE){
                int randomInt = random.nextInt(3) + 1;
                consumeResource(randomInt);
                this.prevMillisPing = System.currentTimeMillis();
            }
            if(this.getState()==State.COORDINATOR && this.getResourceState()==ResourceState.BUSY){
                checkResourceState();
            }
            
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
                        int randomInt = random.nextInt(3) + 1;
                        System.out.println("\u2705\uFE0F Resource has been acquired. I'll use the resource for " +randomInt *10+ " seconds. My state is: " + this.getState());
                        this.initResoruceTime = System.currentTimeMillis();
                        consumeResource(randomInt);
                        break;
                    case RESOURCES_BUSY:
                        this.setState(State.EXECUTOR_IDLE);
                        System.out.println("\u274C\uFE0F Resource has not been acquired. My state is: " + this.getState());
                        int randomWaitTime = random.nextInt(5) + 1;
                        System.out.println("\uD83D\uDE34 I'll wait " + randomWaitTime + " seconds until I 'll make new request");
                        sleeping(randomWaitTime);
                        break;
                    case PING_RESOURCE:
                        System.out.println("Ping, ok");
                        this.lastPingReceived = System.currentTimeMillis();
                        break;
                    default:
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
        System.out.println("\n");
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
