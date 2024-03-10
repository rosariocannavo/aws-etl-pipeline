package com.example.flink;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 * SensorDataDateBucketAssigner implements the Flink BucketAssigner interface.
 * 
 * This class includes utilities necessary to add and configure an S3 bucket as a FileSink for Flink.
 * 
 */
public class SensorDataDateBucketAssigner implements BucketAssigner<ResultData, String> {
    private final String prefix;
    private final String partitionFormat;
    private transient DateTimeFormatter dtFormatForWrite;

    public SensorDataDateBucketAssigner(String partitionFormat, String prefix) {
        this.prefix = prefix;
        this.partitionFormat = partitionFormat;
    }

    @Override
    public String getBucketId(ResultData resultData, Context context) {
        this.dtFormatForWrite = DateTimeFormatter.ofPattern(partitionFormat);

        String eventTimeStr = Long.toString(resultData.getTimestamp());
        LocalDateTime eventTime = LocalDateTime.parse(eventTimeStr.replace(" ", "T"));

        String formattedDate = eventTime.format(this.dtFormatForWrite);

        return String.format("%sts=%s",
                prefix,
                formattedDate
        );
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
