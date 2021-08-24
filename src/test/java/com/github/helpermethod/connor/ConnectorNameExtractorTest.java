package com.github.helpermethod.connor;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ConnectorNameExtractorTest {
	@Test
	void should_extract_connector_name_from_key() throws IOException {
		var connectorNameExtractor = new ConnectorNameExtractor();
		var connectorName = connectorNameExtractor.extract("[\"jdbc-source\", {}]");

		assertThat(connectorName).isEqualTo("jdbc-source");
	}

	@Test
	void should_throw_a_runtime_exception_when_the_json_is_malformed() {
		var connectorNameExtractor = new ConnectorNameExtractor();

		assertThatThrownBy(() -> connectorNameExtractor.extract("\"jdbc-source\", {}")).isInstanceOf(RuntimeException.class);
	}
}
