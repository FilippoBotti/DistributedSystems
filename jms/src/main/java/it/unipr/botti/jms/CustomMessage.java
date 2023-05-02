package it.unipr.botti.jms;

import java.io.Serializable;

public class CustomMessage implements Serializable{
    private static final long serialVersionUID = 1L;
    private int senderId;
    private String text;
    private MessageType messageType;

    public CustomMessage(int senderId, String text, MessageType messageType){
        this.senderId = senderId;
        this.text = text;
        this.messageType = messageType;
    }

    public void setSenderId(int senderId){
        this.senderId = senderId;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setMessageType(MessageType messageType){
        this.messageType = messageType;
    }

    public int getSenderId(){
        return this.senderId;
    }

    public String getText(){
        return this.text;
    }

    public MessageType getMessageType(){
        return this.messageType;
    }



    @Override
    public String toString() {
        String toReturn = this.getSenderId() + ": " + this.getText() + ". Type: " + this.getMessageType();
        return toReturn;
    }
}
