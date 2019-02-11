package com.networknt.kafka.streams;

public interface ChainStreams {
    /**
     * Start the streams processing. The ip and port is for remote queries if the data is not
     * on the current instance.
     *
     * @param ip ip address of the instance
     * @param port port number that the service is bound
     */
    void start(String ip, int port);
    void close();
}
