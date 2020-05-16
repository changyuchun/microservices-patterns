package com.ftgo.restaurantservice.service;

import io.eventuate.tram.events.publisher.TramEventsPublisherConfiguration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ftgo.common.configuration.CommonConfiguration;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
@EntityScan
@Import({ TramEventsPublisherConfiguration.class, CommonConfiguration.class })
public class RestaurantServiceDomainConfiguration {
	@Bean
	public RestaurantService restaurantService() {
		return new RestaurantService();
	}
}
