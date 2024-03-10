package com.example.flink;

/**
 * 
 * ResultData map the results of the transformation, including hour timestamp and hourly average.
 * 
 */
public class ResultData {

    private Long timestamp;
    
    private Double avgTemperature;

    ResultData() { }

    public ResultData(Long timestamp, Double avgTemperature) {
        this.timestamp = timestamp;
        this.avgTemperature = avgTemperature;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getAvgTemperature() {
        return avgTemperature;
    }

    public void setAvgTemperature(Double avgTemperature) {
        this.avgTemperature = avgTemperature;
    }
}
