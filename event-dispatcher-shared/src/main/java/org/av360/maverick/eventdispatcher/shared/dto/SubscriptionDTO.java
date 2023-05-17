package org.av360.maverick.eventdispatcher.shared.dto;

import org.av360.maverick.eventdispatcher.shared.domain.Subscription;

import java.time.LocalDateTime;
import java.util.Map;


public class SubscriptionDTO {
    public record Request (String addressable, Map<String, String> filters) {}


    public record Response (Long identifier, LocalDateTime creationDate, String addressable, Map<String, String> filters) {
        public static Response from(Subscription sub) {
            return new Response(sub.getId(), sub.getCreationDate(), sub.getAddressable(), sub.getFilters());
        }
    }

    public record ShortResponse (Long identifier, LocalDateTime creationDate, String addressable) {
        public static ShortResponse from(Subscription sub) {
            return new ShortResponse(sub.getId(), sub.getCreationDate(), sub.getAddressable());
        }
    }
}
