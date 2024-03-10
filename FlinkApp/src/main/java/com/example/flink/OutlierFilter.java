package com.example.flink;

import org.apache.flink.api.common.functions.FilterFunction;
/*
 *
 * OutlierFilter class implements the Flink FilterFunction interface.
 * 
 * This class is passed to a filter function to perform boolean filtering on the applied DataStream.
 * 
*/

//TODO: replace hardcoded number with first and third quantile
public class OutlierFilter implements FilterFunction<SensorData> {
    @Override
    public boolean filter(SensorData data) {
        return (data.getTemperature() < 30.00 && data.getTemperature() > 18.00);
    }
}

