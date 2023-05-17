package org.av360.maverick.eventdispatcher.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public class Delivery {
    private static final Logger log = LoggerFactory.getLogger(Delivery.class);

    public static Mono<Boolean> deliver(Long subscriptionId, String cloudevent) {
        String url = SubscriptionManager.getInstance().getSubscriberUrl(subscriptionId);

        if (url == null) {
            log.info("No subscription for " + subscriptionId);
            return Mono.empty();
        }

        return HttpClient.create()
                .headers(headers -> headers.add("Content-Type", "application/json"))
                .post()
                .uri(url)
                .send((request, outbound) -> outbound.sendString(Mono.just(cloudevent)))
                .responseSingle((response, byteBufMono) -> Mono.just(response.status().code()))
                .map(statusCode -> {
                    if (statusCode / 100 == 2) {
                        log.debug("Delivered to " + url + " with status code " + statusCode + " for subscription " + subscriptionId);
                        return true;
                    } else {
                        log.error("Error delivering to " + url + " with status code " + statusCode + " for subscription " + subscriptionId);
                        return false;
                    }
                })
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(throwable -> {
                    log.error("Error delivering to " + url + " for subscription " + subscriptionId, throwable);
                    return Mono.just(false);
                });
    }
}