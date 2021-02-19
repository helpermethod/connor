package com.github.helpermethod.connect.offset.reset;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

class ConnectOffsetMapper {
    private final ObjectMapper objectMapper;

    ConnectOffsetMapper() {
        objectMapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
    }

    public <T> T readValue(byte[] src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return objectMapper.readValue(src, valueType);
    }
}