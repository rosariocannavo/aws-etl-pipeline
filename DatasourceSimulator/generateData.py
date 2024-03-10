import boto3
import json

stream_name = 'TemperatureKinesisDataStream'
partition_key = 'samplepartitionkey'
region_name = 'eu-north-1'
aws_access_key_id = 'your_acces_key_id'
aws_secret_access_key = 'your_aws_secret_access_key'

def put_record_to_kinesis(data):
    kinesis = boto3.client('kinesis', region_name=region_name, aws_access_key_id=aws_access_key_id, aws_secret_access_key=aws_secret_access_key)
    record = {
        'Data': json.dumps(data),
        'PartitionKey': partition_key
    }
    response = kinesis.put_record(
        StreamName=stream_name,
        Data=record['Data'],
        PartitionKey=record['PartitionKey']
    )
    print(response)

def main():
    try:
        with open('data.json', 'r') as file:
            data_points = json.load(file)

        for data in data_points:
            print(data)
            put_record_to_kinesis(data)
    except Exception as e:
        print(f"Error during read of file: {e}")

if __name__ == "__main__":
    main()
