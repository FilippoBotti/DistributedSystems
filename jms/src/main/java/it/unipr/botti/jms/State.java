package it.unipr.botti.jms;

public enum State {
    COORDINATOR,
    CANDIDATE,
    WAITING_FOR_COORDINATOR,
    EXECUTOR,
    WAITING_FOR_RESOURCES,
}
