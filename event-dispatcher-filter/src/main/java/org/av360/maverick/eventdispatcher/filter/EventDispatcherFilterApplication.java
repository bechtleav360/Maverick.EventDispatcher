package org.av360.maverick.eventdispatcher.filter;

import org.av360.maverick.eventdispatcher.filter.rabbit.RabbitMQClassic;
import org.av360.maverick.eventdispatcher.filter.rabbit.RabbitMQStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.TimeoutException;


@SpringBootApplication
public class EventDispatcherFilterApplication {
    public static void main(String[] args) throws IOException, TimeoutException {
        Logger log = LoggerFactory.getLogger(EventDispatcherFilterApplication.class);

        SpringApplication.run(EventDispatcherFilterApplication.class, args);

        log.info("Starting Grpc Subscription Refresh Task");
        Timer timer = new Timer();
        timer.schedule(new RefreshGrpcSubscriptionsTask(), 0, 5 * 60 * 1000);

        RabbitMQClassic.getInstance().init();
        RabbitMQStream.getInstance().init();
    }
}
