package com.example.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * SensorData class map the ingested data provided in the following json format:
 *      {"timestamp": 1682331404, "sensor-id": 1, "temperature": 21.1}
 * 
 * To mantain the naming while manipulating the data, Jackson is used for the name mapping
 * 
 */
public class SensorData {
    
    private long timestamp;
    
    @JsonProperty("sensor-id")
    private int sensorId;

    private double temperature;

    public SensorData() { }

    public SensorData(
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("sensor-id") int sensorId,
            @JsonProperty("temperature") double temperature) {
        this.timestamp = timestamp;
        this.sensorId = sensorId;
        this.temperature = temperature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "timestamp=" + timestamp +
                ", sensorId=" + sensorId +
                ", temperature=" + temperature +
                '}';
    }
}
