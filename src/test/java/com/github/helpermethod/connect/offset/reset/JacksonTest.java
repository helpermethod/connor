package com.github.helpermethod.connect.offset.reset;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;

@DisplayNameGeneration(ReplaceUnderscores.class)
public class JacksonTest {
	@Test
	public void should_map_array_elements_to_fields() throws IOException {
		var connectOffsetKeyMapper = new ConnectOffsetKeyMapper();
		var key = connectOffsetKeyMapper.map("[\"jdbc-source\", {}]".getBytes(UTF_8));

		assertThat(key.connector).isEqualTo("jdbc-source");
	}
}
