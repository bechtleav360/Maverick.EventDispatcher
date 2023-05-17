package org.av360.maverick.eventdispatcher.publisher;

import org.av360.maverick.eventdispatcher.shared.domain.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);
    private static SubscriptionManager instance = null;
    private HashMap<Long, String> subscriptions = new HashMap<>();
    private ConsumerManager consumerManager;

    private SubscriptionManager() {
        consumerManager = ConsumerManager.getInstance();
    }

    public static SubscriptionManager getInstance() {
        if (instance == null) {
            instance = new SubscriptionManager();
        }
        return instance;
    }

    public void addSubscription(Long subscriptionId, String subscriberUrl) {
        if (hasSubscription(subscriptionId)) {
            removeSubscription(subscriptionId);
        }

        if (consumerManager.createConsumer(subscriptionId) == null) {
            return;
        }

        subscriptions.put(subscriptionId, subscriberUrl);
    }

    public String getSubscriberUrl(Long subscriptionId) {
        return subscriptions.get(subscriptionId);
    }

    public void refreshSubscriptions(List<Subscription> subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (!hasSubscription(subscription.getId())) {
                addSubscription(subscription.getId(), subscription.getAddressable());
            }
        }

        for (Long subscriptionId : this.subscriptions.keySet()) {
            if (subscriptions.stream().noneMatch(s -> s.getId().equals(subscriptionId))) {
                removeSubscription(subscriptionId);
            }
        }
    }

    public void removeSubscription(Long subscriptionId) {
        consumerManager.removeConsumer(subscriptionId);
        subscriptions.remove(subscriptionId);
    }

    public boolean hasSubscription(Long subscriptionId) {
        return subscriptions.containsKey(subscriptionId);
    }
}
