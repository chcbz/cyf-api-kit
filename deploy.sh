#!/bin/bash -l
JARFILE=$(ls -l |grep  .jar | awk '{print$9}')
FILEPATH=$(pwd "$JARFILE")"/"$JARFILE
PID=$(ps -ef|grep -w "$FILEPATH" | grep -v grep |awk '{printf $2}')

if [ ! -d "./logs" ]; then
  echo $FILEPATH
  mkdir ./logs
fi

if [ ! -n "$PID" ]; then
    echo "pid is null"
    nohup $JAVA_HOME/bin/java -jar $FILEPATH > $(pwd "$JARFILE")/logs/startlog.log &
    exit
else
    echo "pid not null"
fi

kill -9 ${PID}

if [ $? -eq 0 ];then
    echo "kill $JARFILE success"
    nohup $JAVA_HOME/bin/java -jar $FILEPATH >$(pwd "$JARFILE")/logs/startlog.log &
else
    echo "kill $JARFILE fail"
fi