package com.example.notification_service.config;

import org.springframework.amqp.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQconfig {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQconfig.class);

    public static final String EXCHANGE = "notification.exchange";
    // normal queue
    public static final String MAIN_QUEUE = "notification.queue";
    public static final String MAIN_ROUTING_KEY = "notification.routing.key";
    // retry
    public static final String RETRY_EXCHANGE = "notification.retry.exchange";
    public static final String RETRY_QUEUE = "notification.retry.queue";
    public static final String RETRY_ROUTING_KEY = "notification.retry.key";
    // Holds messages for N time using TTL
    public static final String DEAD_LETTER_EXCHANGE = "notification.dlx";
    public static final String DEAD_LETTER_QUEUE = "notification.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "notification.dlk";

    // Use the value rabbitmq if not use this default value
    @Value("${rabbitmq.retry.ttl-ms:60000}")
    private long ttl;

    // Main
    @Bean
    TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    // Send to the dlx
    public Queue mainQueue() {
        return QueueBuilder.durable(MAIN_QUEUE)
                .deadLetterExchange(RETRY_EXCHANGE) // Failed messages (retry)
                .deadLetterRoutingKey(RETRY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(mainQueue())
                .to(notificationExchange())
                .with(MAIN_ROUTING_KEY);
    }

    // Retry

    @Bean
    TopicExchange retryExchange() {
        return new TopicExchange(RETRY_EXCHANGE, true, false);
    }

    @Bean
    // Wait 300000ms (5 minutes) then resend to the main queue
    public Queue retryQueue() {
        return QueueBuilder.durable(RETRY_QUEUE)
                .ttl(300000)
                .deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(MAIN_ROUTING_KEY)
                .build();
    }

    @Bean

    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(retryExchange())
                .with(RETRY_ROUTING_KEY);
    }

    // Dead letter
    @Bean
    TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    // Message converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Producer
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        // Confirm Callback - If the message was not received by any queue
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message was not confirmed: {}", cause);
            }
        });

        //
        template.setReturnsCallback(returned -> log.error(
                "Message returned: exchange={}, routingKey={}",
                returned.getExchange(),
                returned.getRoutingKey()));

        return template;
    }

    // Consumer
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3); // Multiple consumers
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }
}