package it.unipr.botti.jms;

public enum State {
    COORDINATOR,
    CANDIDATE,
    WAITING_FOR_COORDINATOR,
    EXECUTOR_IDLE,
    WAITING_FOR_RESOURCE,
    EXECUTOR_WITH_RESOURCE,
    DEAD
}
