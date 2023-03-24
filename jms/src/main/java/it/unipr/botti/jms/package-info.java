/**
 *
 * Provides three examples describing the use of the JMS communication library.
 *
 * The first JMS example is based on two processes defined by the Receiver and
 * Sender classes and shows how implement the point-to-point communication.
 *
 * The Receiver process needs to be started before the Sender process.
 *
 * The Receiver process can be replaced by a QueueListener process that also
 * shows how to use message listeners.
 *
 * The second JMS example is based on two processes defined by the Subscriber
 * and Publisher classes and shows how implement the point-to-point
 * communication.
 *
 * The Subscriber process needs to be started before the Publisher process.
 *
 * The Subscriber process can be replaced by a TopicListener process that also
 * shows how to use message listeners.
 *
 * The third JMS example is based on two processes defined by the Server and
 * Client classes and shows how to implement a request-reply protocol.
 *
 * The Server process needs to be started before the Client process.
 *
**/
package it.unipr.botti.jms;
