package com.sophos.reactive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.sophos.reactive.beans.LogRegistry;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${reactive.logger.queue:reactor.rabbitmq}")
	private String queue;

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
		amqpAdmin.declareQueue(new Queue(queue, true, false, false));
	}

	@PreDestroy
	public void close() throws Exception {
		Objects.requireNonNull(connectionMono.block()).close();
	}

	public Flux<OutboundMessageResult> send(LogRegistry logRequest) {
		return Mono.fromCallable(() -> new OutboundMessage("", queue, objectMapper.writeValueAsBytes(logRequest)))
			.flatMapMany(outboundMessage -> sender.sendWithPublishConfirms(Flux.just(outboundMessage)))
			.filter(OutboundMessageResult::isAck)
		;
	}

}
