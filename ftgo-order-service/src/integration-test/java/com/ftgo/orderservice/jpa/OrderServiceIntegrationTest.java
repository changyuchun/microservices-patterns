package com.ftgo.orderservice.jpa;

import com.ftgo.common.model.Money;
import com.ftgo.consumerservice.api.ConsumerServiceChannels;
import com.ftgo.consumerservice.api.command.ValidateOrderByConsumerCommand;
import com.ftgo.orderservice.OrderDetailsMother;
import com.ftgo.orderservice.RestaurantMother;
import com.ftgo.orderservice.command.OrderServiceCommandHandlersConfiguration;
import com.ftgo.orderservice.controller.model.MenuItemIdAndQuantity;
import com.ftgo.orderservice.domain.OrderServiceWebConfiguration;
import com.ftgo.orderservice.message.OrderServiceMessageConfiguration;
import com.ftgo.orderservice.message.TestMessageConsumer2;
import com.ftgo.orderservice.model.Order;
import com.ftgo.orderservice.repository.OrderRepository;
import com.ftgo.orderservice.repository.RestaurantRepository;
import com.ftgo.orderservice.service.OrderService;
import com.ftgo.restaurantservice.api.event.RestaurantCreatedEvent;
import com.ftgo.restaurantservice.api.model.MenuItem;
import com.ftgo.restaurantservice.api.model.RestaurantMenu;
import com.ftgo.testutil.FtgoTestUtil;
import com.jayway.jsonpath.JsonPath;

import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.inmemory.TramInMemoryConfiguration;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.testutil.TestMessageConsumerFactory;
import io.eventuate.util.test.async.Eventually;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

import java.util.Collections;
import java.util.function.Predicate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrderServiceIntegrationTest.TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderServiceIntegrationTest {
	public  static final String RESTAURANT_ID                 = "1";
	private static final String CHICKED_VINDALOO_MENU_ITEM_ID = "1";
	
	@Value("${local.server.port}")
	private int port;
	
	@Autowired
	private DomainEventPublisher domainEventPublisher;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	@Qualifier("mockConsumerService")
	private TestMessageConsumer2 mockConsumerService;

	private String baseUrl(String path) {
		return "http://localhost:" + port + path;
	}

	@Configuration
	@EnableAutoConfiguration
	@Import({ OrderServiceWebConfiguration.class,
			OrderServiceMessageConfiguration.class,
			OrderServiceCommandHandlersConfiguration.class,
			TramCommandProducerConfiguration.class,
			TramInMemoryConfiguration.class })
	public static class TestConfiguration {
		@Bean
		public ChannelMapping channelMapping() {
			return new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
		}

		@Bean
		public TestMessageConsumerFactory testMessageConsumerFactory() {
			return new TestMessageConsumerFactory();
		}

		@Bean
		public DataSource dataSource() {
			EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
			return builder.setType(EmbeddedDatabaseType.H2)
					.addScript("eventuate-tram-embedded-schema.sql")
					.addScript("eventuate-tram-sagas-embedded.sql").build();
		}

		@Bean
		public TestMessageConsumer2 mockConsumerService() {
			return new TestMessageConsumer2("mockConsumerService",
					ConsumerServiceChannels.consumerServiceChannel);
		}
	}

	 @Test
	  public void shouldCreateOrder() {
	    domainEventPublisher.publish("net.chrisrichardson.ftgo.restaurantservice.domain.Restaurant", RESTAURANT_ID,
	            Collections.singletonList(new RestaurantCreatedEvent("Ajanta", RestaurantMother.RESTAURANT_ADDRESS,
	                    new RestaurantMenu(Collections.singletonList(new MenuItem(CHICKED_VINDALOO_MENU_ITEM_ID, "Chicken Vindaloo", new Money("12.34")))))));

		Eventually.eventually(() -> {
			FtgoTestUtil.assertPresent(restaurantRepository.findById(Long.parseLong(RESTAURANT_ID)));
		});

		long consumerId = 1511300065921L;

		Order order = orderService.createOrder(consumerId, Long.parseLong(RESTAURANT_ID), OrderDetailsMother.DELIVERY_INFORMATION, Collections.singletonList(new MenuItemIdAndQuantity(CHICKED_VINDALOO_MENU_ITEM_ID, 5)));

		FtgoTestUtil.assertPresent(orderRepository.findById(order.getId()));

		String expectedPayload = "{\"consumerId\":1511300065921,\"orderId\":1,\"orderTotal\":\"61.70\"}";

		Message message = mockConsumerService
				.assertMessageReceived(commandMessageOfType(
						ValidateOrderByConsumerCommand.class.getName()).and(
						withPayload(expectedPayload)));

		System.out.println("message=" + message);

	}

	private Predicate<? super Message> withPayload(String expectedPayload) {
		return (m) -> expectedPayload.equals(m.getPayload());
	}

	private Predicate<Message> forConsumer(long consumerId) {
		return (m) -> {
			Object doc = com.jayway.jsonpath.Configuration
					.defaultConfiguration().jsonProvider()
					.parse(m.getPayload());
			Object s = JsonPath.read(doc, "$.consumerId");
			return new Long(consumerId).equals(s);
		};
	}

	private Predicate<Message> commandMessageOfType(String commandType) {
		return (m) -> m.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE).equals(commandType);
	}
}