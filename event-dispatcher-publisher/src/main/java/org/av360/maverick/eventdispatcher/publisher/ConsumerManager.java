package org.av360.maverick.eventdispatcher.publisher;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.rabbitmq.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

public class ConsumerManager {

    private static final Logger log = LoggerFactory.getLogger(ConsumerManager.class);
    private static ConsumerManager instance = null;
    private final ConnectionFactory connectionFactory;
    private Receiver receiver;
    private HashMap<Long, Disposable> disposables = new HashMap<>();

    private ConsumerManager() {
        Config cfg = Config.getInstance();

        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(cfg.host());
        connectionFactory.setPort(cfg.amqpPort());
        connectionFactory.setUsername(cfg.user());
        connectionFactory.setPassword(cfg.password());
        connectionFactory.setVirtualHost(cfg.virtualHost());

        log.info("Initializing RabbitMQ connection");
        receiver = RabbitFlux.createReceiver(new ReceiverOptions().connectionFactory(connectionFactory));
    }

    public static ConsumerManager getInstance() {
        if (instance == null) {
            instance = new ConsumerManager();
        }
        return instance;
    }

    private Disposable _createConsumer(Long subscriptionId) {
        String queueName = "sub_" + subscriptionId;

        return receiver.consumeManualAck(queueName)
                .concatMap(delivery -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    log.debug("Received '" + message + "'");

                    String url = SubscriptionManager.getInstance().getSubscriberUrl(subscriptionId);

                    if (url == null) {
                        log.info("No subscription for " + subscriptionId);
                        delivery.nack(true);
                        return Mono.empty();
                    }

                    return HttpClient.create()
                            .headers(headers -> headers.add("Content-Type", "application/json"))
                            .post()
                            .uri(url)
                            .send((request, outbound) -> outbound.sendString(Mono.just(message)))
                            .responseSingle((response, byteBufMono) -> Mono.just(response.status().code()))
                            .flatMap(statusCode -> {
                                if (statusCode / 100 == 2) {
                                    log.debug("Delivered to " + url + " with status code " + statusCode + " for subscription " + subscriptionId);
                                    delivery.ack();
                                } else {
                                    log.error("Error delivering to " + url + " with status code " + statusCode + " for subscription " + subscriptionId);
                                    delivery.nack(true);
                                    return Mono.delay(Duration.ofSeconds(5)).then(Mono.empty());
                                }
                                return Mono.empty();
                            })
                            .timeout(Duration.ofSeconds(10))
                            .onErrorResume(throwable -> {
                                log.error("Error delivering to " + url + " for subscription " + subscriptionId, throwable);
                                delivery.nack(true);
                                return Mono.empty();
                            });
                })
                .subscribe();
    }

    public Disposable createConsumer(Long subscriptionId) {
        if (disposables.containsKey(subscriptionId)) {
            log.info("Consumer already exists. Removing anyway.");
            removeConsumer(subscriptionId);
        }

        Disposable disposable = _createConsumer(subscriptionId);

        disposables.put(subscriptionId, disposable);
        return disposable;
    }

    public void removeConsumer(Long subscriptionId) {
        Disposable disposable = disposables.get(subscriptionId);

        log.info("Disposing and removing consumer " + subscriptionId);
        disposable.dispose();
        disposables.remove(subscriptionId);
    }
}