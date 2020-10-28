package com.sophos.logger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import com.sophos.logger.beans.LogRequest;
import com.sophos.logger.beans.LogResponse;
import com.sophos.logger.repository.LoggerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Service
public class LoggerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerService.class);
	private final LoggerRepository loggerRepository;
	private final RabbitService rabbitService;
	private final ObjectMapper objectMapper;
	private final EmitterProcessor<LogResponse> processor;
	private final FluxSink<LogResponse> sink;

	public LoggerService(LoggerRepository loggerRepository, RabbitService rabbitService) {
		this.loggerRepository = loggerRepository;
		this.rabbitService = rabbitService;

		objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();

		processor = EmitterProcessor.create();
		sink = processor.sink();
	}

	public Flux<LogResponse> getAll() {
		return loggerRepository.getAll();
	}

	public Flux<LogResponse> stream() {
		return processor
			.doOnNext(logResponse -> LOGGER.info("Sink: {}", logResponse.getCodigo()))
		;
	}

	@PostConstruct
	public void ingest() {
		rabbitService.receive()
			.doOnNext(this::logDelivery)
			.flatMap(delivery -> Mono.fromCallable(() -> objectMapper.readValue(delivery.getBody(), LogRequest.class)))
			.flatMap(loggerRepository::save)
			.doOnNext(this::logSaving)
			.doOnNext(sink::next)
			.subscribe()
		;
	}

	private void logDelivery(Delivery delivery) {
		LOGGER.info(
			"Mensaje recibido -> Cola: {} - Mensaje: {}",
				delivery.getEnvelope().getRoutingKey(),
				new String(delivery.getBody(), StandardCharsets.UTF_8)
		);
	}

	private void logSaving(LogResponse logResponse) {
		LOGGER.info(
			"Log registrado con éxito -> Tipo: {} - ID: {}",
				logResponse.getMetodo(),
				logResponse.getCodigo()
		);
	}

}
