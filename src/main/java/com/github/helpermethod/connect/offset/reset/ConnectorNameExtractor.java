package com.github.helpermethod.connect.offset.reset;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;

class ConnectorNameExtractor {
    private final JSON json;

    ConnectorNameExtractor() {
        json = new JSON();
    }

    String extract(byte[] key) {
        try {
            return json.arrayOfFrom(String.class, key)[0];
        } catch (IOException e) {
            throw new AssertionError("Should never happen", e);
        }
    }
}
