package com.sophos.logger.controller;

import com.sophos.logger.beans.LogResponse;
import com.sophos.logger.service.LoggerService;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/logger")
@CrossOrigin
public class LoggerController {

	private final LoggerService loggerService;

	public LoggerController(LoggerService loggerService) {
		this.loggerService = loggerService;
	}

	@GetMapping("")
	public Flux<LogResponse> getAll() {
		return loggerService.getAll();
	}

	@GetMapping("/sse")
	public Flux<ServerSentEvent<LogResponse>> stream() {
		return Flux.interval(Duration.ofSeconds(1))
			.onBackpressureDrop()
			.flatMap(interval -> loggerService.stream())
			.map(logResponse ->
				ServerSentEvent.<LogResponse>builder()
					.data(logResponse)
					.id(UUID.randomUUID().toString())
					.build()
			)
		;
	}

}
