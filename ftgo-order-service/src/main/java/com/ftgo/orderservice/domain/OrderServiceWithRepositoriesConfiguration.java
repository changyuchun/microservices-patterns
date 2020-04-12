package com.ftgo.orderservice.domain;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.ftgo.orderservice.service.OrderServiceConfiguration;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@Import({ OrderServiceConfiguration.class })
public class OrderServiceWithRepositoriesConfiguration {

}