package org.av360.maverick.eventdispatcher.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.av360.maverick.eventdispatcher.filter.rabbit.RabbitMQClassic;
import org.av360.maverick.eventdispatcher.shared.domain.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    public static Mono<Void> dispatch(String cloudevent) {
        return Mono.defer(() -> {
            List<Subscription> foundSubscriptions;
            try {
                foundSubscriptions = SubscriptionManager.getInstance().findSubscriptions(cloudevent);
            } catch (JsonProcessingException e) {
                log.error("Error parsing CloudEvent", e);
                return Mono.empty();
            }

            RabbitMQClassic rabbitMQClassic = RabbitMQClassic.getInstance();

            return Flux.fromIterable(foundSubscriptions)
                    .flatMap(subscription -> sendEvent(rabbitMQClassic, "sub_" + subscription.getId(), cloudevent))
                    .then();
        });
    }

    private static Mono<Void> sendEvent(RabbitMQClassic rabbitMQClassic, String queue, String cloudevent) {
        return rabbitMQClassic.publish(queue, cloudevent)
                .flatMap(success -> {
                    if (success) {
                        return Mono.empty();
                    } else {
                        log.error("Error publishing CloudEvent to subscription " + queue);
                        return Mono.delay(Duration.ofSeconds(5))
                                .then(sendEvent(rabbitMQClassic, queue, cloudevent));
                    }
                });
    }
}