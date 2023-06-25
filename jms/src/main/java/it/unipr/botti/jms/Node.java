package it.unipr.botti.jms;

public class Node {

    private int id;
    private Receiver receiver;
    private Sender sender;
    private State state;
    private int coordinatorId;

    public Node(final int id){
        this.id = id;
        this.receiver = new Receiver(id);
        this.sender = new Sender(id);
        this.state = State.CANDIDATE;
        this.coordinatorId = -1;
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
                            this.setState(State.EXECUTOR);
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
                            sender.sendMessageToAll();
                        }
                        break;
                }
            }
            System.out.println("Election phase is done. My state is: " + this.state);
            executionPhase();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void executionPhase(){
        if (this.id==0){
            sender.sendResourcesMessage(this.coordinatorId);
            this.setState(State.WAITING_FOR_RESOURCES);
        }
        while(true){
            CustomMessage receivedMessage = receiver.receiveResourcesMessage(2000);
            switch (receivedMessage.getMessageType()){
                case NEW_COORDINATOR:
                    if (this.id > receivedMessage.getSenderId()){
                        sender.sendElectionMessage();
                        receivedMessage = receiver.receiveElectionMessage(5000);
                    }
                    else {
                        System.out.println("Received new coordinator");
                        this.coordinatorId = receivedMessage.getSenderId();
                        this.setState(State.EXECUTOR);
                        System.out.println("Election phase is done. My state is: " + this.state);
                    }
                    break;
                case TIMEOUT_RESOURCES:
                    if(this.state==State.WAITING_FOR_RESOURCES){
                        System.out.println("Timeout: coordinator is off, new election needed");
                        this.coordinatorId = -1;
                        this.setState(State.CANDIDATE);
                        election();
                    }
                    break;
                case ELECTION:
                    this.coordinatorId = -1;
                    this.setState(State.CANDIDATE);
                    sender.sendAcknowledgment(receivedMessage.getSenderId(), MessageType.ELECTION_ACKNOWLEDGMENT);
                    election();
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

    public static void main (final String[] args) {
       
        int id = Integer.parseInt(args[0]);
        Node n = new Node(id);

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
       
    }
}
