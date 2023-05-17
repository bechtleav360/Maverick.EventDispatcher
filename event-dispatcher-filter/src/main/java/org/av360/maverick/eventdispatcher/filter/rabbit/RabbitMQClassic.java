package org.av360.maverick.eventdispatcher.filter.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import org.av360.maverick.eventdispatcher.filter.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;


public class RabbitMQClassic {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQClassic.class);
    private static RabbitMQClassic instance;

    private ConnectionFactory connectionFactory = new ConnectionFactory();
    private Sender sender;

    private RabbitMQClassic() {
    }

    public static RabbitMQClassic getInstance() {
        if (instance == null) {
            instance = new RabbitMQClassic();
        }
        return instance;
    }

    public void init() {
        log.info("Initializing RabbitMQ AMQP connection");
        Config cfg = Config.getInstance();

        connectionFactory.setHost(cfg.host());
        connectionFactory.setPort(cfg.amqpPort());
        connectionFactory.setUsername(cfg.user());
        connectionFactory.setPassword(cfg.password());
        connectionFactory.setVirtualHost(cfg.virtualHost());

        sender = RabbitFlux.createSender(new SenderOptions().connectionFactory(connectionFactory));

        log.info("RabbitMQ AMQP connection initialized");
    }

    public Mono<Boolean> publish(String queue, String message) {
        return sender.declareQueue(QueueSpecification.queue(queue).durable(true))
                .then(sender.send(Mono.just(
                        new OutboundMessage("", queue, message.getBytes())
                )))
                .thenReturn(true)
                .onErrorResume(e -> {
                    log.error("Error publishing message", e);
                    return Mono.just(false);
                });
    }

    public Mono<Void> createQueue(String queue) {
        return sender.declareQueue(QueueSpecification.queue(queue).durable(true)).then();
    }

    public Mono<Void> deleteQueue(String queue) {
        return sender.deleteQueue(QueueSpecification.queue(queue), true, true).then();
    }

    public void close() {
        if (sender != null) {
            sender.close();
        }
    }
}
