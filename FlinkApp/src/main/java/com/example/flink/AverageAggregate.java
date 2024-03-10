package com.example.flink;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * 
 * AverageAggregate class extends the Flink AggregateFunction interface
 * 
 * This class takes an input data stream of SensorData and calculates the average temperature and time average. 
 * It can be used within an aggregate operation on the dataset that you want to transform. 
 * The output will be a ResultData type data stream containing the temperatures averages.
 * 
 * The Parameters for AggregateFunction are InputType, AggregatorType and OutputType.
 */
public class AverageAggregate implements AggregateFunction<SensorData, Tuple3<Long,Double, Double>, ResultData> {
    @Override
    public Tuple3<Long, Double, Double> createAccumulator() {
        return new Tuple3<>(0L, 0.0, 0.0);
    }

    @Override
    public Tuple3<Long, Double, Double> add(SensorData value, Tuple3<Long,Double, Double> accumulator) {
        return new Tuple3<>(accumulator.f0 + value.getTimestamp(), accumulator.f1 + value.getTemperature(), accumulator.f2 + 1L);
    }

    @Override
    public ResultData getResult(Tuple3<Long, Double, Double> accumulator) {
        return new ResultData((long)( accumulator.f0 / accumulator.f2),((double) accumulator.f1) / accumulator.f2);
    }

    @Override
    public Tuple3<Long, Double, Double> merge(Tuple3<Long, Double, Double> a, Tuple3<Long, Double, Double> b) {
        return new Tuple3<>(a.f0 + b.f0, a.f1 + b.f1, a.f2 + b.f2);
    }
}