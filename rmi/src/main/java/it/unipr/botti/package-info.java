/**
 *
 * Provides four examples describing the use of the RMI communication library.
 *
 * The first RMI example is based on two processes defined by the
 * TemperatureServer and TemperatureClient classes and the TemperatureServer
 * provides temperature information to the TemperatureClient process.
 * The TemperatureServer process needs to be started before the
 * TemperatureClient process.
 *
 * The second RMI example is based on two processes defined by the
 * CallbackServer and CallbackClient classes and the CallbackServer
 * provides temperature information and the CallbackClient process
 * passes it a TemperaturWriter object for receiving such an information.
 * The CallbackServer process needs to be started before the
 * CallbackClient process.
 *
**/
package it.unipr.botti;
