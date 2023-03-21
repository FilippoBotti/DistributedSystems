package it.unipr.botti.socket;
import java.io.Serializable;

/**
 * Message describes the message from client to the server.
 * 
 * @author      Filippo Botti <filippo.botti2@studenti.unipr.it>
 * @author      Simone Montali <simone.montali1@studenti.unipr.it>
 * 
 * @version     1.0
 * @since       1.0
 */

public class Message implements Serializable {
    private MessageType requestType;
    private Serializable messageObject;

    private static final long serialVersionUID = 1L;

    /**
     * This constructor is the empty constructor for the Message
     */
    
    public Message() {
    }

    /**
     * This constructor sets every parameters of the message
     *  
     * @param MessageType  requestType
     * @param Serializable messageObject
     */
    
    public Message(MessageType requestType, Serializable messageObject ) {
        this.requestType = requestType;
        this.messageObject = messageObject;
    }
    

    /**
     * This method gets the Message's RequestType
     * @return MessageType requestType
     */
    
    public MessageType getRequestType() {
        return requestType;
    }

    /**
     * This method sets the Message's requestType
     * @param MessageType requestType
     */
    
    public void setRequestType(MessageType requestType) {
        this.requestType = requestType;
    }


    /**
     * This method gets the Message's object
     * @return Serializable newObject
     */
    
    public Serializable getMessageObject() {
        return messageObject;
    }
    
    /**
     * This method sets the Message's object
     * @param Serializable newObject
     */
    
    public void setMessageObject(Serializable messageObject) {
        this.messageObject = messageObject;
    }
    
    /**
     * This method gets the Message's serialVersionUID
     * @return long serialVersionUID
     */
    
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    

}