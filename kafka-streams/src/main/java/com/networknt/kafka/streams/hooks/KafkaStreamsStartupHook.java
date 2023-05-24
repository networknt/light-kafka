package com.networknt.kafka.streams.hooks;

import com.networknt.server.Server;
import com.networknt.server.StartupHookProvider;
import com.networknt.utility.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaStreamsStartupHook implements StartupHookProvider {

    private static Logger logger= LoggerFactory.getLogger(KafkaStreamsStartupHook.class);
    public static GenericStreamsTransformerLifecycle genericStreamsTransformerLifecycle = new GenericStreamsTransformerLifecycle();

    @Override
    public void onStartup() {
        logger.info("KafkaStreamsStartupHook Starting !!! ");

        int port = Server.getServerConfig().getHttpsPort();
        String ip = NetUtils.getLocalAddressByDatagram();
        logger.info("ip = {} port = {}",ip,  port);
        // start the kafka stream process
        genericStreamsTransformerLifecycle.start(ip, port);
        logger.info("KafkaStreamsStartupHook onStartup ends.");
    }

}
