# -*- coding: utf-8 -*-

import airflow
from airflow.operators.bash_operator import BashOperator
from airflow.models import DAG
from datetime import timedelta

dag_dir = '/usr/local/airflow/dags/pingdom_reports/'
jar = dag_dir + 'pingdom_reports-0.2.0-standalone.jar'
pingdom_user = 'USERNAME'
pingdom_password = 'PASSWORD'
pingdom_api_key = 'API-KEY'
recipients = 'user@example.com'

args = {
    'owner': 'airflow',
    'start_date': airflow.utils.dates.days_ago(0)
}

dag = DAG(
    dag_id='pingdom_reponse_time_report', default_args=args,
    schedule_interval='0 0 * * *',
    dagrun_timeout=timedelta(minutes=60))

reports = [['us-web-icmp', 'US web (ICMP)', 'greenyellow', 'Pingdom Response Times - US web (ICMP)'],
           ['us-web-http', 'US web (HTTP)', 'violet', 'Pingdom Response Times - US web (HTTP)'],
           ['us-web-https', 'US web (HTTPS)', 'cyan', 'Pingdom Response Times - US web (HTTPS)']]

cmd_template = 'java -jar {3} -u {4} -p {5} -a {6} -n "{0}" -c {1} > {7} && < {7} gnuplot {8} && biabam {9} -s "{2}" "{10}"'
common_args = [jar, pingdom_user, pingdom_password, pingdom_api_key, 'pingdom.dat', 'pingdom_report.gp', 'pingdom_report.png', recipients]

for r in reports:
    task_id = r[0]
    check_name = r[1]
    color = r[2]
    subject = r[3]
    task = BashOperator(
        task_id=task_id,
        queue='java',
        bash_command=cmd_template.format(check_name, color, subject, *common_args),
        dag=dag)

if __name__ == "__main__":
    dag.cli()
