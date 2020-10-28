package com.sophos.logger.service;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;

@Service
public class RabbitService {

	@Value("${reactive.logger.queue:reactor.rabbitmq}")
	private String queue;

	private final AmqpAdmin amqpAdmin;
	private final Mono<Connection> connectionMono;
	private final Receiver receiver;


	public RabbitService(AmqpAdmin amqpAdmin, Mono<Connection> connectionMono, Receiver receiver) {
		this.amqpAdmin = amqpAdmin;
		this.connectionMono = connectionMono;
		this.receiver = receiver;
	}

	@PostConstruct
	public void init() {
		amqpAdmin.declareQueue(new Queue(queue, false, false, true));
	}

	@PreDestroy
	public void close() throws Exception {
		Objects.requireNonNull(connectionMono.block()).close();
	}

	public Flux<Delivery> receive() {
		return receiver.consumeAutoAck(queue);
	}

}
