package com.github.helpermethod.connect.offset.reset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.ARRAY;

@JsonFormat(shape = ARRAY)
@JsonPropertyOrder(alphabetic = true)
class Key {
    final String connector;

    @JsonCreator
    Key(@JsonProperty("connector") String connector) {
        this.connector = connector;
    }
}
