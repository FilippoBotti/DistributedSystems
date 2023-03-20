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
    private Serializable newObject;

    private static final long serialVersionUID = 1L;

    /**
     * This constructor is the empty constructor for the Message
     */
    
    public Message() {
    }

    /**
     * This constructor sets every parameters of the message
     *  
     * @param String       username
     * @param String       passwordHash
     * @param MessageType  requestType
     * @param String       query
     * @param Serializable newObject
     */
    
    public Message(MessageType requestType, Serializable newObject ) {
        this.requestType = requestType;
        this.newObject = newObject;
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
    
    public Serializable getNewObject() {
        return newObject;
    }
    
    /**
     * This method sets the Message's object
     * @param Serializable newObject
     */
    
    public void setNewObject(Serializable newObject) {
        this.newObject = newObject;
    }
    
    /**
     * This method gets the Message's serialVersionUID
     * @return long serialVersionUID
     */
    
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    

}