package org.av360.maverick.eventdispatcher.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDispatcherReceiverApplication {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(EventDispatcherReceiverApplication.class);
        Config cfg = Config.getInstance();


        log.info("Starting EventDings Reception application");

        log.info("Initializing RabbitMQ connection");
        RabbitMQHandler.getInstance().init();
        log.info("RabbitMQ connection initialized");

        log.info("Starting Webserver");
        WebhookApi.start(cfg.serverPort(), "/webhook");
        log.info("Webserver started");
    }
}