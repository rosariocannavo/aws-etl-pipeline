# AWS ETL PIPELINE
The goal of this pipeline is to collect temperatures from sensors distributed in an outdoor environment. The messages are provided in JSON format and follow the following pattern: 
```
{"timestamp": 1682331404, "sensor-id": 1, "temperature": 21.1}
```
The system filters out temperature values that are outside the range bounded by the first and third quartile (outliers due to false readings) and then calculates the hourly average of the obtained temperatures and reports it on an S3 bucket in parquet format.

## System Architecture

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1AHq5AD9ehn0Ay9vNDMeTkAfpsfAm5Ibf" alt="Alt Text">
</div>


The adopted ETL system leverages **AWS Kinesis** as the entry point for real-time data acquisition. The data is then processed through a Java **Flink** Cluster running on top of **AWS Managed Apache Flink**, enabling real-time transformations and application of business logic. The results are subsequently stored inside an **AWS S3** bucket in **Parquet** format for durable retention and easy access. This managed solution provides dynamic scalability and simplifies infrastructure management. All the infrastructure is seamlessly orchestrated and managed with **AWS CDK**, simplifying the deployment and maintenance of cloud resources.

### System choices
For the creation of this pipeline, I chose to use Kinesis and not MSK because given the nature of the data and in view of the following stream analysis, Kinesis seemed to me to be the most suitable solution to accomplish the task. In addition, the choice of Kinesis as the ingestion tool meant that it was a natural implementation of the transform phase through KDA (since a few versions renamed Amazon Managed System for Apache Flink ) as it represents together with Kinesis a powerful tool for data transformation and is more straightforward to manage than an AWS Glue service. The choice of Managed Apache Flink later resulted in the use of the Flink Framework to perform the analysis, as opposed to other frameworks such as Spark, which turns out not to be supported by this service. For testing purpose I also made a simple script that is able to send data to kinesis so as to simulate real time ingestion and test the architecture.

#### AWS CDK
AWS CDK, or Cloud Development Kit, is an open-source development tool. It allows developers to define cloud infrastructure resources using familiar programming languages instead of manually writing resource code. In this project, I utilized CDK to automatically create a Kinesis instance, an MAF instance, and an S3 bucket. Python was chosen as the scripting language. CDK generated a CloudFormation template facilitating easy tiering up and tiering down from the terminal. Regarding the Flink application, passing the code is streamlined during configuration: simply provide the JAR file's path, and it will be automatically uploaded to a designated S3 bucket and retrieved by MAF.

#### AWS Kinesis
Amazon Kinesis is an AWS service designed for real-time processing of streaming data. It enables users to ingest, process, and analyze large volumes of data in real-time, allowing for quick and dynamic responses to streaming information. 

#### AWS Managed Apache Flink
AWS Managed Apache Flink is a fully managed, serverless stream processing service offered by Amazon Web Services (AWS). It enables users to build and run Apache Flink applications without the need to manage the underlying infrastructure. 

#### AWS Bucket
Amazon S3, part of Amazon Web Services (AWS), is a secure, highly available, and cost-effective object storage service. 
#### Apache Flink vs Apache Spark
Apache Flink and Apache Spark are powerful big data processing frameworks, each with its own strengths.

* **Processing Model:**
  - *Flink:* Specializes in seamless stream processing, extending to batch processing.
  - *Spark:* Initially tailored for batch processing, later embracing streaming via Structured Streaming.

* **Event Time Processing:**
  - *Flink:* Excels in efficient event time processing.
  - *Spark:* Incorporates support for event time processing through Structured Streaming.

* **State Management:**
  - *Flink:* Excels in efficient and scalable distributed stateful processing.
  - *Spark:* Possesses stateful processing capabilities, albeit with potentially more intricate state management.

* **Latency:**
  - *Flink:* Showcases lower end-to-end latency in stream processing scenarios.
  - *Spark:* Introduces slightly higher latency due to its micro-batch processing model.



## System Requirements

To set up and run this project, ensure that the following prerequisites are installed on your machine:

- [Node.js](https://nodejs.org/): Version > 18.00
- [npm](https://www.npmjs.com/): Node.js package manager 
- [Python](https://www.python.org/): Version > 3.0
- [AWS CLI](https://aws.amazon.com/cli/): AWS Command Line Interface
- [AWS CDK](https://aws.amazon.com/cdk/): Cloud Development Kit
- [Java](https://www.java.com/): Version > 11.0
- [Apache Maven](https://maven.apache.org/): A software project management and comprehension tool

Once installed, you can proceed with setting up and running the project.

## Run the Project

* Clone the project

  ```bash
    git clone https://github.com/rosariocannavo/aws-etl-pipeline
  ```

* Build the Apache Flink JAR by running the following Maven command inside the *FlinkApp* directory:
  
   ```bash
   mvn clean package
  ```

* Place the generated JAR file inside the *cdk_config/code-assets* directory.

* Make sure you are logged in through your AWS CLI to your Amazon account. 

* Run the following command to bootstrap your AWS CDK environment inside the cdk-config directory:
  ```bash
  cdk bootstrap
  ```

* Deploy the infrastructure by running the following CDK command:
  ```bash
  cdk deploy
  ```

* Simulate data streaming through the provided Python script:
  ```bash
  python3 simulate.py
  ```

* Check the data flowing inside the bucket by the online dashboard or aws cli using:
  ```bash
  aws s3 ls s3://my-kinesis-s3-bucket
  ```

* To tear down the infrastructure, run the following CDK command:
  ```bash
  cdk destroy
  ```
### Important Notes

Please note that the project is currently only a preliminary prototype, and for this reason, it is not intended for use in a real production environment. In fact, the security policy aspect is not yet addressed, and it has not been tested in a real-world scenario.





