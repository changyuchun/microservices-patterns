package com.ftgo.orderservice.configuration;

import io.eventuate.tram.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcher;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.eventuate.tram.sagas.participant.SagaParticipantConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.ftgo.common.configuration.CommonConfiguration;
import com.ftgo.orderservice.api.OrderServiceChannels;
import com.ftgo.orderservice.command.OrderServiceCommandHandlers;

/**
 * The configuration class to instantiate and wire the command handler.
 * 
 * @author  Wuyi Chen
 * @date    05/07/2020
 * @version 1.0
 * @since   1.0
 */
@Configuration
@Import({ SagaParticipantConfiguration.class, TramEventsPublisherConfiguration.class, CommonConfiguration.class })
public class OrderServiceCommandConfiguration {
	@Bean
	public OrderServiceCommandHandlers orderCommandHandlers() {
		return new OrderServiceCommandHandlers();
	}

	@Bean
	public SagaCommandDispatcher orderCommandHandlersDispatcher(OrderServiceCommandHandlers orderCommandHandlers, SagaCommandDispatcherFactory sagaCommandDispatcherFactory) {
	    return sagaCommandDispatcherFactory.make(OrderServiceChannels.ORDER_SERVICE_COMMAND_CHANNEL, orderCommandHandlers.commandHandlers());
	}
}
