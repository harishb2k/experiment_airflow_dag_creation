from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.python_operator import PythonOperator, BranchPythonOperator
from datetime import datetime, timedelta
from airflow.models import Variable
from airflow.utils.trigger_rule import TriggerRule 
 
# Step 1 - define the default parameters for the DAG
default_args = {
  'owner': 'airflow',
  'depends_on_past': False,
  'start_date': datetime(2019, 7, 20),
  'email': ['vipin.chadha@gmail.com'],
  'email_on_failure': False,
  'email_on_retry': False,
  'retries': 1,
  'retry_delay': timedelta(minutes=5),

}

dag = DAG(  'hello_harish',
        schedule_interval='0 0 * * *' ,
        default_args=default_args
    )

source_read_from_redshift_1 = BashOperator(
  task_id= 'source_read_from_redshift_1',
  bash_command="echo do it source_read_from_redshift_1",
  dag=dag
)

source_read_from_redshift_2 = BashOperator(
  task_id= 'source_read_from_redshift_2',
  bash_command="echo do it source_read_from_redshift_2",
  dag=dag
)

source_read_from_redshift_3 = BashOperator(
  task_id= 'source_read_from_redshift_3',
  bash_command="echo do it source_read_from_redshift_3",
  dag=dag
)

sink_2__needs__transformation_2 = BashOperator(
  task_id= 'sink_2__needs__transformation_2',
  bash_command="echo do it sink_2__needs__transformation_2",
  dag=dag
)

sink_1__needs__transformation_1 = BashOperator(
  task_id= 'sink_1__needs__transformation_1',
  bash_command="echo do it sink_1__needs__transformation_1",
  dag=dag
)

start = BashOperator(
  task_id= 'start',
  bash_command="echo do it start",
  dag=dag
)

end = BashOperator(
  task_id= 'end',
  bash_command="echo do it end",
  dag=dag
)

transformation_1__needs_source_read_from_redshift_1 = BashOperator(
  task_id= 'transformation_1__needs_source_read_from_redshift_1',
  bash_command="echo do it transformation_1__needs_source_read_from_redshift_1",
  dag=dag
)

transformation_2__needs_source_read_from_redshift_2_and_3 = BashOperator(
  task_id= 'transformation_2__needs_source_read_from_redshift_2_and_3',
  bash_command="echo do it transformation_2__needs_source_read_from_redshift_2_and_3",
  dag=dag
)

end.set_upstream([sink_2__needs__transformation_2, sink_1__needs__transformation_1]) 
sink_2__needs__transformation_2.set_upstream(transformation_2__needs_source_read_from_redshift_2_and_3)
transformation_2__needs_source_read_from_redshift_2_and_3.set_upstream([source_read_from_redshift_2, source_read_from_redshift_3]) 
source_read_from_redshift_2.set_upstream(start)
source_read_from_redshift_3.set_upstream(start)
sink_1__needs__transformation_1.set_upstream(transformation_1__needs_source_read_from_redshift_1)
transformation_1__needs_source_read_from_redshift_1.set_upstream(source_read_from_redshift_1)
source_read_from_redshift_1.set_upstream(start)
