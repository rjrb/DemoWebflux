package com.sophos.reactive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.sophos.reactive.beans.LogRequest;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.OutboundMessageResult;
import reactor.rabbitmq.Sender;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;

@Service
public class RabbitService {

	private static final String QUEUE = "reactor.rabbitmq";

	private final AmqpAdmin amqpAdmin;
	private final Mono<Connection> connectionMono;
	private final Sender sender;

	private final ObjectMapper objectMapper;

	public RabbitService(AmqpAdmin amqpAdmin, Mono<Connection> connectionMono, Sender sender) {
		this.amqpAdmin = amqpAdmin;
		this.connectionMono = connectionMono;
		this.sender = sender;

		objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
	}

	@PostConstruct
	public void init() {
		amqpAdmin.declareQueue(new Queue(QUEUE, false, false, true));
	}

	@PreDestroy
	public void close() throws Exception {
		Objects.requireNonNull(connectionMono.block()).close();
	}

	public Flux<OutboundMessageResult> send(LogRequest logRequest) {
		return Mono.fromCallable(() -> new OutboundMessage("", QUEUE, objectMapper.writeValueAsBytes(logRequest)))
			.flatMapMany(outboundMessage -> sender.sendWithPublishConfirms(Flux.just(outboundMessage)))
			.filter(OutboundMessageResult::isAck)
		;
	}

}
