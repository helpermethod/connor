package com.github.helpermethod.connect.offset.reset;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

class ConnectOffsetKeyMapper {
    private final ObjectMapper objectMapper;

    ConnectOffsetKeyMapper() {
        objectMapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
    }

    Key map(byte[] src) throws IOException {
        return objectMapper.readValue(src, Key.class);
    }
}
