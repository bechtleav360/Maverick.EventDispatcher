package org.av360.maverick.eventdispatcher.filter.rabbit;

import com.rabbitmq.stream.*;
import org.av360.maverick.eventdispatcher.filter.Config;
import org.av360.maverick.eventdispatcher.filter.Dispatcher;
import org.av360.maverick.eventdispatcher.filter.SubscriptionManager;
import org.av360.maverick.eventdispatcher.shared.CloudEventValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class RabbitMQStream {
    private static RabbitMQStream instance;

    private Environment environment;

    private static final Logger log = LoggerFactory.getLogger(RabbitMQStream.class);

    private RabbitMQStream() {
    }

    public static RabbitMQStream getInstance() {
        if (instance == null) {
            instance = new RabbitMQStream();
        }
        return instance;
    }

    public void init() {
        log.info("Initializing RabbitMQ Stream connection");
        Config cfg = Config.getInstance();

        environment = Environment.builder()
                .host(cfg.host())
                .username(cfg.user())
                .password(cfg.password())
                .port(cfg.streamPort())
                .virtualHost(cfg.virtualHost())
                .build();

        Consumer consumer = this.environment.consumerBuilder()
                .stream(Config.getInstance().stream())
                .offset(OffsetSpecification.first())
                .messageHandler((offset, message) -> {
                    String msg = message.getBody().toString();

                    //TODO: This is a hack to remove the "Data{" and "}" from the message
                    if (msg.startsWith("Data{")) {
                        msg = msg.substring(5, msg.length() - 1);
                    }

                    String finalMsg = msg;
                    Mono.fromRunnable(() -> {
                                while (!SubscriptionManager.getInstance().hasSubscriptions()) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .then(Mono.fromCallable(() -> CloudEventValidator.isValidCloudEvent(finalMsg)))
                            .filter(valid -> valid)
                            .flatMap(valid -> Dispatcher.dispatch(finalMsg))
                            .subscribe();
                })
                .build();

        log.info("RabbitMQ Stream connection initialized");
    }
}
