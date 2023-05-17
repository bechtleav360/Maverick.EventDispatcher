package org.av360.maverick.eventdispatcher.shared.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Subscription {

    @Id
    private Long id;
    private LocalDateTime creationDate;
    private String addressable;
    private Map<String, String> filters;


    public Subscription(String addressable, Map<String, String> filters) {
        this.addressable = addressable;
        this.filters = filters;
        this.creationDate = LocalDateTime.now();
    }
}

