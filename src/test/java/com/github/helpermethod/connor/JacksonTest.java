package com.github.helpermethod.connor;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;

@DisplayNameGeneration(ReplaceUnderscores.class)
class JacksonTest {
	@Test
	void should_extract_connector_name_from_key() throws IOException {
		var connectorNameExtractor = new ConnectorNameExtractor();
		var connectorName = connectorNameExtractor.extract("[\"jdbc-source\", {}]");

		assertThat(connectorName).isEqualTo("jdbc-source");
	}
}
