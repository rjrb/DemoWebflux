package com.sophos.logger.repository;

import com.sophos.logger.beans.LogRequest;
import com.sophos.logger.beans.LogResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class LoggerRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerRepository.class);
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static final String TABLA_DYNAMO = "Logger";
	private final DynamoDbAsyncClient dynamoDbAsyncClient;

	public LoggerRepository(DynamoDbAsyncClient dynamoDbAsyncClient) {
		this.dynamoDbAsyncClient = dynamoDbAsyncClient;
	}

	public Flux<LogResponse> getAll() {
		final QueryRequest queryRequest = QueryRequest.builder()
			.tableName(TABLA_DYNAMO)
			.keyConditionExpression("tipo = :tipo")
			.projectionExpression("fecha, responsable, metodo, codigo, entidad")
			.expressionAttributeValues(
				Map.of(
					":tipo", AttributeValue.builder().s("LOG").build()
				)
			)
			.scanIndexForward(false)
			.limit(10)
			.build()
		;

		return Mono.from(dynamoDbAsyncClient.queryPaginator(queryRequest))
			.flatMapIterable(
				response -> response.items().stream().map(
					item -> {
						final LogResponse logResponse = new LogResponse();
						logResponse.setFecha(LocalDateTime.parse(item.get("fecha").s(), DTF));
						logResponse.setResponsable(item.get("responsable").s());
						logResponse.setMetodo(item.get("metodo").s());
						logResponse.setCodigo(item.get("codigo").s());
						return logResponse;
					}
				)
				.collect(Collectors.toList())
			)
		;
	}

	public Mono<LogResponse> save(LogRequest logRequest) {
		final LocalDateTime ahora =  LocalDateTime.now();

		final PutItemRequest putItemRequest = PutItemRequest.builder()
			.tableName(TABLA_DYNAMO)
			.item(
				Map.of(
					"tipo", AttributeValue.builder().s("LOG").build(),
					"fecha", AttributeValue.builder().s(ahora.format(DTF)).build(),
					"responsable", AttributeValue.builder().s(logRequest.getResponsable()).build(),
					"metodo", AttributeValue.builder().s(String.valueOf(logRequest.getMetodo())).build(),
					"codigo", AttributeValue.builder().s(String.valueOf(logRequest.getCodigo())).build(),
					"entidad", AttributeValue.builder().s(logRequest.getEntidad().toString()).build(),
					"ttl", AttributeValue.builder().n(String.valueOf(LocalDateTime.now().plusDays(30).toEpochSecond(ZoneOffset.ofHours(-5)))).build()
				)
			)
			.build()
		;

		return Mono.fromCompletionStage(dynamoDbAsyncClient.putItem(putItemRequest))
			.doOnError(e -> LOGGER.error("Error registrando log en DynamoDB: {}", e.getLocalizedMessage()))
			.onErrorStop()
			.map(putItemResponse -> mapLogRequestToLogResponse(logRequest, ahora))
		;
	}

	private LogResponse mapLogRequestToLogResponse(LogRequest logRequest, LocalDateTime ahora) {
		final LogResponse logResponse = new LogResponse();
		logResponse.setFecha(ahora);
		logResponse.setResponsable(logRequest.getResponsable());
		logResponse.setMetodo(logRequest.getMetodo());
		logResponse.setCodigo(logRequest.getCodigo());
		return logResponse;
	}

}
