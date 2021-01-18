package com.github.helpermethod.kafka.connect.offset.reset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;

@DisplayNameGeneration(ReplaceUnderscores.class)
public class JacksonTest {
	@Test
	public void should_map_array_elements_to_fields() throws JsonProcessingException {
		var objectMapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
		var key = objectMapper.readValue("[\"jdbc-source\", {}]", Key.class);

		assertThat(key.connector).isEqualTo("jdbc-source");
	}
}
