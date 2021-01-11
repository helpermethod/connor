package com.github.helpermethod.kafka.connect.reset;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class JacksonTest {
	@Test
	public void should_map_array_elements_to_fields() throws JsonProcessingException {
		var objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var key = objectMapper.readValue("[\"jdbc-source\", {}]", Key.class);

		assertThat(key.connector).isEqualTo("jdbc-source");
	}
}
