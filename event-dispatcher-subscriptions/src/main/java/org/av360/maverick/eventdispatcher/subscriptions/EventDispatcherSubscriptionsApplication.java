package org.av360.maverick.eventdispatcher.subscriptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@EnableR2dbcRepositories
@SpringBootApplication
public class EventDispatcherSubscriptionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventDispatcherSubscriptionsApplication.class, args);
	}

}
