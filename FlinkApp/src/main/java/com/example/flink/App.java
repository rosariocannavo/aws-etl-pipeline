package com.example.flink;

import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;

import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import java.io.IOException;
import java.time.Duration;

import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;

import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.formats.parquet.avro.AvroParquetWriters;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;

import com.amazonaws.services.kinesisanalytics.flink.connectors.config.AWSConfigConstants;
import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;

import org.apache.flink.core.fs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * App class is the main component for the developed Flink application. It includes methods for managing Flink and AWS configurations,
 * ingesting data with Kinesis, loading data into the S3 bucket, and transforming data using Flink primitives. 
 * Additionally, the application's main function handles the Flink process.
 * 
 */

public class App {
	private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private static final String KINESIS_STREAM_NAME = "TemperatureKinesisDataStream";
	private static final String AWS_REGION = "eu-north-1";
	private static final String STREAM_INITIAL_POSITION = "LATEST";
	private static final String S3_DEST_KEY = "my-kinesis-s3-bucket";
	private static final String SINK_PARALLELISM_KEY = "1";
	private static final String PARTITION_FORMAT_KEY = "none";
	private static final String FLINK_APPLICATION_PROPERTIES = "BlueprintMetadata";

    private static boolean isLocal(StreamExecutionEnvironment env) {
		return env instanceof LocalStreamEnvironment;
	}
	
	// Configuration managemente function
    private static Properties getAppProperties() throws IOException {
		// Note: this won't work when running locally
		Map<String, Properties> applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
		Properties flinkProperties = applicationProperties.get(FLINK_APPLICATION_PROPERTIES);

		if(flinkProperties == null) {
			LOG.error("Unable to retrieve " + FLINK_APPLICATION_PROPERTIES + "; please ensure that you've " +
					"supplied them via application properties.");
			return null;
		}

		if(!flinkProperties.containsKey(KINESIS_STREAM_NAME)) {
			LOG.error("Unable to retrieve property: " + KINESIS_STREAM_NAME);
			return null;
		}

		if(!flinkProperties.containsKey(AWS_REGION)) {
			LOG.error("Unable to retrieve property: " + AWS_REGION);
			return null;
		}

		if(!flinkProperties.containsKey(STREAM_INITIAL_POSITION)) {
			LOG.error("Unable to retrieve property: " + STREAM_INITIAL_POSITION);
			return null;
		}

		if(!flinkProperties.containsKey(S3_DEST_KEY)) {
			LOG.error("Unable to retrieve property: " + S3_DEST_KEY);
			return null;
		}

		if(!flinkProperties.containsKey(PARTITION_FORMAT_KEY)) {
			LOG.error("Unable to retrieve property: " + PARTITION_FORMAT_KEY);
			return null;
		}

		return flinkProperties;
	}

    private static FlinkKinesisConsumer<SensorData> getKinesisSource(StreamExecutionEnvironment env, Properties appProperties) {

		String streamName = "TemperatureKinesisDataStream";
		String regionStr = "eu-north-1";
		String streamInitPos = "LATEST";

		if(!isLocal(env)) {
			streamName = appProperties.get(KINESIS_STREAM_NAME).toString();
			regionStr = appProperties.get(AWS_REGION).toString();
			streamInitPos = appProperties.get(STREAM_INITIAL_POSITION).toString();
		}

		// Set the connection Kinesis connection policy
		Properties consumerConfig = new Properties();
		consumerConfig.put(AWSConfigConstants.AWS_REGION, regionStr);
		consumerConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, streamInitPos);
		consumerConfig.put(ConsumerConfigConstants.RECORD_PUBLISHER_TYPE,
						   ConsumerConfigConstants.RecordPublisherType.POLLING.name());

		DeserializationSchema<SensorData> deserializationSchema = new SensorDataDeserializationSchema();
		
		// Attach Kinesis to A FlinkConsumer which could read from the stream
		FlinkKinesisConsumer<SensorData> kinesisSensoDataSource = new FlinkKinesisConsumer<>(streamName,
																					deserializationSchema,
																					consumerConfig);
		return kinesisSensoDataSource;
	}

	// Set a Flink FileSink attached to an S3 bucket
    private static FileSink<ResultData> getFileSink(StreamExecutionEnvironment env, Properties appProperties) {
		String outputPath = "flink-output/";
		if(!isLocal(env)) {
			outputPath = appProperties.get(S3_DEST_KEY).toString();
		}

		String partitionFormat = "yyyy-MM-dd-HH";
		if(!isLocal(env)) {
			partitionFormat = appProperties.get(PARTITION_FORMAT_KEY).toString();
		}

		Path path = new Path(outputPath);

		String prefix = String.format("%sjob_start=%s/", "app-kinesis-to-s3", System.currentTimeMillis());

		// Specify the sink to write in parquet format
		final FileSink<ResultData> sink = FileSink
				.forBulkFormat(path, AvroParquetWriters.forReflectRecord(ResultData.class))
				.withBucketAssigner(new SensorDataDateBucketAssigner(partitionFormat, prefix))
				.withRollingPolicy(OnCheckpointRollingPolicy.build())
				.build();

		return sink;
	}


	// Computation function
    private static void runAppWithKinesisSource(StreamExecutionEnvironment env, Properties appProperties) {

        // Get the connection object to Kinesis and map the input flux to a Flink DataStream 
        FlinkKinesisConsumer<SensorData> sensorDataSource = getKinesisSource(env, appProperties);
        DataStream<SensorData> sensorStream = env.addSource(sensorDataSource);

        // Source processing here.

		// Apply timestamp and watermark to the ingested data
        DataStream<SensorData> sensorDataStreamTimestamp = 
			sensorStream
				.assignTimestampsAndWatermarks(WatermarkStrategy
        		.<SensorData>forBoundedOutOfOrderness(Duration.ofSeconds(30))  // Place watermark with specified watermarking strategy
        		.withTimestampAssigner((event, timestamp) -> event.getTimestamp())); // Use the timestamp provided with the data as Flink timestamp
    

		// Filter the outliers
		DataStream<SensorData> filteredDataStream = 
        	sensorDataStreamTimestamp
            	.filter(new OutlierFilter());


		// Calculate Hourly mean
        DataStream<ResultData> hourlyAverageStream = 
            filteredDataStream  
				.windowAll(TumblingEventTimeWindows.of(Time.hours(1)))  // Specify window size
				.aggregate(new AverageAggregate());
           
                        
        // Get the resource and write output datastream to the s3 sink
        FileSink<ResultData> fSink = getFileSink(env, appProperties);
        DataStreamSink<ResultData> sink = hourlyAverageStream.sinkTo(fSink).name("S3 File Sink");

        if(!isLocal(env) && appProperties.containsKey(SINK_PARALLELISM_KEY)) {
            int sinkParallelism = Integer.parseInt(appProperties.get(SINK_PARALLELISM_KEY).toString());
            sink.setParallelism(sinkParallelism);
        }        

	}

    public static void main(String[] args) throws Exception {
		// Set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		Properties appProperties = null;
		if(!isLocal(env)) {
			appProperties = getAppProperties();
			if(appProperties == null) {
				LOG.error("Incorrectly specified application properties. Exiting...");
				return;
			}
		}

		// Start the processing
		runAppWithKinesisSource(env, appProperties);

		env.execute("Kinesis Data Streams to S3 Flink Streaming App");
	} 

}

