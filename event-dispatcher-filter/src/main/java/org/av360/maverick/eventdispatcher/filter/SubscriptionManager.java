package org.av360.maverick.eventdispatcher.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.av360.maverick.eventdispatcher.filter.rabbit.RabbitMQClassic;
import org.av360.maverick.eventdispatcher.shared.domain.Subscription;
import org.av360.maverick.eventdispatcher.shared.grpc.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


public final class SubscriptionManager {

    private static SubscriptionManager INSTANCE;

    private List<Subscription> subscriptions = new ArrayList<Subscription>();

    private SubscriptionManager() {
    }

    public static SubscriptionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SubscriptionManager();
        }

        return INSTANCE;
    }

    public SubscriptionId addSubscription(SubscriptionMessage subscriptionMessage) {
        if (subscriptions.stream().anyMatch(subscription -> subscription.getId().equals(subscriptionMessage.getId()))) {
            deleteSubscription(SubscriptionId.newBuilder().setId(subscriptionMessage.getId()).build(), false);
        }

        subscriptions.add(
                new Subscription(
                        subscriptionMessage.getId(),
                        LocalDateTime.ofEpochSecond(subscriptionMessage.getCreatedAt().getSeconds(), subscriptionMessage.getCreatedAt().getNanos(), ZoneOffset.UTC),
                        subscriptionMessage.getSubscriberUri(),
                        subscriptionMessage.getFiltersMap()
                )
        );

        RabbitMQClassic.getInstance().createQueue("sub_" + subscriptionMessage.getId());

        return SubscriptionId.newBuilder().setId(subscriptionMessage.getId()).build();
    }

    public SubscriptionId deleteSubscription(SubscriptionId subscriptionId, boolean deleteQueue) {
        subscriptions.removeIf(subscription -> subscription.getId().equals(subscriptionId.getId()));

        if (deleteQueue) {
            RabbitMQClassic.getInstance().deleteQueue("sub_" + subscriptionId.getId());
        }

        return subscriptionId;
    }

    public void deleteAllSubscriptions(boolean deleteQueues) {
        if (deleteQueues) {
            for (Subscription subscription : subscriptions) {
                RabbitMQClassic.getInstance().deleteQueue("sub_" + subscription.getId());
            }
        }

        subscriptions.clear();
    }

    public List<Subscription> findSubscriptions(String cloudevent) throws JsonProcessingException {
        List<Subscription> matchingSubscriptions = new ArrayList<Subscription>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(cloudevent);

        for (Subscription subscription : subscriptions) {
            boolean allFiltersFound = true;
            for (Map.Entry<String, String> entry : subscription.getFilters().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (!jsonNode.has(key) || !jsonNode.get(key).asText().equals(value)) {
                    allFiltersFound = false;
                    break;
                }
            }

            if (allFiltersFound) {
                matchingSubscriptions.add(subscription);
            }
        }

        return matchingSubscriptions;
    }

    public boolean hasSubscriptions() {
        return !subscriptions.isEmpty();
    }
}
