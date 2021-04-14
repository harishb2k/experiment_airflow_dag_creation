### About
This program take a DAG input as json and converts it to Airflow file.

#### Run
```shell
mvn clean install 
java -cp "target/test-1.0-SNAPSHOT-jar-with-dependencies.jar" com.batch.Main           
```

It will generate out.py - you can put this file to airflow Dag folder to execute.

![alt text](https://github.com/harishb2k/images/blob/master/airflow_auto_1.png?raw=true)
![alt text](https://github.com/harishb2k/images/blob/master/airflow_auto_2.png?raw=true)

#### Graph Json
The file /src/main/resources/graph.json contains the json of this graph. You can edit to change it.

```json
{
  "start": {
    "id": "start",
    "type": "no_op"
  },
  "source": [
    {
      "id": "source_read_from_redshift_1",
      "type": "read_from_redshift",
      "depends_on": [
        "start"
      ],
      "config": {
        "redshift": {
          "host": "redshift.internal",
          "query": "select * from table_user"
        }
      }
    },
    {
      "id": "source_read_from_redshift_2",
      "type": "read_from_redshift",
      "depends_on": [
        "start"
      ],
      "config": {
        "redshift": {
          "host": "redshift.internal",
          "query": "select * from table_order"
        }
      }
    },
    {
      "id": "source_read_from_redshift_3",
      "type": "read_from_redshift",
      "depends_on": [
        "start"
      ],
      "config": {
        "redshift": {
          "host": "redshift.internal",
          "query": "select * from table_card"
        }
      }
    }
  ],
  "transformation": [
    {
      "id": "transformation_1__needs_source_read_from_redshift_1",
      "depends_on": [
        "source_read_from_redshift_1"
      ],
      "type": "docker_execute",
      "config": {
        "docker": {
          "image": "custom_image:1.0.0",
          "volumn": "${b4b0535a-f87d-49fd-885f-7e237e557cf4}.output.s3"
        }
      }
    },
    {
      "id": "transformation_2__needs_source_read_from_redshift_2_and_3",
      "depends_on": [
        "source_read_from_redshift_2",
        "source_read_from_redshift_3"
      ],
      "type": "spark_execute",
      "config": {
        "spark": {
          "program": "s3://.../program.jar"
        }
      }
    }
  ],
  "sink": [
    {
      "id": "sink_1__needs__transformation_1",
      "depends_on": [
        "transformation_1__needs_source_read_from_redshift_1"
      ],
      "type": "map_s3_to_athena",
      "config": {
        "athena": {
          "input_bucket": "${5fc684e4-cfd1-44f0-842b-f62fa9163327}.output.s3",
          "column_defination": "id int, ...",
          "table_name": "table_1234"
        }
      }
    },
    {
      "id": "sink_2__needs__transformation_2",
      "depends_on": [
        "transformation_2__needs_source_read_from_redshift_2_and_3"
      ],
      "type": "map_s3_to_athena",
      "config": {
        "athena": {
          "input_bucket": "${de2f0dfd-ea15-4220-8b6b-0b48a011c29c}.output.s3",
          "column_defination": "id int, ...",
          "table_name": "table_1234_abcd"
        }
      }
    }
  ],
  "end": {
    "id": "end",
    "depends_on": [
      "sink_1__needs__transformation_1",
      "sink_2__needs__transformation_2"
    ],
    "type": "docker_execute",
    "config": {
      "docker": {
        "image": "custom_image:1.0.0"
      }
    }
  }
}
```