package com.networknt.kafka.common;

/**
 * Command is similar to the request in the request/response but it is asynchronous through Kafka. It
 * is suitable for one service to ask another service to do something. Instead of sending the HTTP
 * request and expect the other party to respond immediately, the other party can do the work in its
 * priority. The best example would be the light-scheduler to ask batch job services for batch job
 * scheduling.
 *
 * @author Steve Hu
 */
public interface Command {
}
