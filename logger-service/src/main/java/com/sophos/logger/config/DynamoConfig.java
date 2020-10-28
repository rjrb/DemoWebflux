package com.sophos.logger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

@Configuration
public class DynamoConfig {

	@Bean
	DynamoDbAsyncClient dynamoDbAsyncClient() {
		return DynamoDbAsyncClient.builder()
			.endpointOverride(URI.create("http://localhost:8000"))
			.build()
		;
	}

}
