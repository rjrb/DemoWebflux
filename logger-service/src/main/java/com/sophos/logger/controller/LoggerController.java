package com.sophos.logger.controller;

import com.sophos.logger.beans.LogResponse;
import com.sophos.logger.service.LoggerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/logger")
public class LoggerController {

	private final LoggerService loggerService;

	public LoggerController(LoggerService loggerService) {
		this.loggerService = loggerService;
	}

	@GetMapping("")
	public Flux<LogResponse> getAll() {
		return loggerService.getAll();
	}

}
