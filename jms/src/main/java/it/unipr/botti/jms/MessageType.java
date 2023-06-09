package it.unipr.botti.jms;

public enum MessageType {
    ELECTION,
    ELECTION_ACKNOWLEDGMENT,
    NEW_COORDINATOR,
    ERROR,
    TIMEOUT_ELECTION,
    ASK_FOR_RESOURCES,
    RESOURCES_ACKNOWLEDGMENT,
    RESOURCES_BUSY,
    TIMEOUT_RESOURCES,
    FREE_RESOURCE,
    PING_RESOURCE
}
