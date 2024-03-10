package com.example.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;

import java.io.IOException;

/**
 * 
 * SensoDataDeserializationSchema class extends the Flink AbstractDeserializationSchema.
 * 
 * This class provides some utilities necessary for connecting with Kinesis, allowing the mapping of the Kinesis stream to a Flink DataStream.
 * 
 */
public class SensorDataDeserializationSchema extends AbstractDeserializationSchema<SensorData> {
    private static final long serialVersionUID = 1L;

    private transient ObjectMapper objectMapper;

    @Override
    public void open(InitializationContext context) {
        objectMapper = JsonMapper.builder().build().registerModule(new JavaTimeModule());
    }

    @Override
    public SensorData deserialize(byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes, SensorData.class);
    }
} // class