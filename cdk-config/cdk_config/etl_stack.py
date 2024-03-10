import aws_cdk as cdk
import aws_cdk.aws_kinesisanalytics_flink_alpha as flink
from aws_cdk import aws_kinesis as kinesis
from aws_cdk import aws_ec2 as ec2
from aws_cdk.aws_s3 import Bucket
from aws_cdk import aws_iam as iam

import os

class KinesisDataStreamStack(cdk.Stack):
    def __init__(self, scope: cdk.App, id: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        # IAM Role for Kinesis Data Stream Access
        kinesis_role = iam.Role(self, "KinesisDataStreamRole",
            assumed_by=iam.ServicePrincipal("flink.amazonaws.com"),
            managed_policies=[
                iam.ManagedPolicy.from_aws_managed_policy_name("AmazonKinesisFullAccess"),
            ],
        )


        # IAM Role for S3 Bucket Access
        s3_role = iam.Role(self, "S3BucketRole",
            assumed_by=iam.ServicePrincipal("flink.amazonaws.com"),
            managed_policies=[
                iam.ManagedPolicy.from_aws_managed_policy_name("AmazonS3FullAccess"),
            ],
        )

        # IAM Role for Flink Application
        flink_role = iam.Role(self, "FlinkApplicationRole",
            assumed_by=iam.ServicePrincipal("flink.amazonaws.com"),
        )

        # Assume the Kinesis role and S3 role in the Flink role's trust policy
        flink_role.add_to_policy(iam.PolicyStatement(
            effect=iam.Effect.ALLOW,
            actions=["sts:AssumeRole"],
            resources=[kinesis_role.role_arn, s3_role.role_arn],
        ))

        
        # Create an AWS Kinesis Data Stream
        stream_name = 'TemperatureKinesisDataStream'  
        number_of_shards = 1  

        kinesis_data_stream = kinesis.Stream(self, 'TemperatureKinesisDataStream',
                                        stream_name=stream_name,
                                        shard_count=number_of_shards)


        # Output for the Kinesis Data Stream ARN
        cdk.CfnOutput(self, 'KinesisDataStreamArn', value=kinesis_data_stream.stream_arn)


        # Create an S3 Output bucket
        s3_bucket = Bucket(self, "S3Bucket",
        bucket_name='my-kinesis-s3-bucket',
        versioned=True,
        removal_policy=cdk.RemovalPolicy.DESTROY,
        auto_delete_objects=True,
        )
       

        # Create an output for the S3 bucket ARN
        cdk.CfnOutput(self, 'S3BucketArn', value=s3_bucket.bucket_arn)

        # Attach IAM roles to Kinesis Data Stream, and S3 bucket
        kinesis_data_stream.grant_read_write(kinesis_role)
        s3_bucket.grant_read_write(s3_role)
        
        
        # Define additional Flink application configuration options
        flink_app_config = {
            "input_stream_name": "TemperatureKinesisDataStream",
            "output_s3_bucket": "my-kinesis-s3-bucket",
            "output_s3_prefix": "flink-output/",
            "checkpoint_interval": "60000",  # Checkpoint interval in milliseconds
            "parallelism": "2",  # Number of parallel subtasks
        }

        # Create a Managed Apache Flink application that take a Flink JAR from the 
        # "/code-asset" directory and load it into a specific s3 bucket managed from MAF.
        flink_app = flink.Application(self, "App",
            code=flink.ApplicationCode.from_asset(os.path.join(os.path.dirname(__file__), "code-asset")),
            runtime=flink.Runtime.FLINK_1_11,
            property_groups={
                "FlinkApplicationProperties": flink_app_config,
            },
            role=flink_role
        )


    





      
