#!/usr/bin/env bash
set -euo pipefail

docker compose exec -T namenode bash -lc '
  hdfs dfs -mkdir -p /test
  printf "hadoop docker hadoop nexus\n" > /tmp/mots.txt
  hdfs dfs -put -f /tmp/mots.txt /test/mots.txt
  hdfs dfs -rm -r -f /test-out >/dev/null 2>&1 || true
  if ! yarn jar /opt/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.6.jar \
    wordcount /test /test-out >/tmp/nexus-wordcount.log 2>&1; then
    tail -n 40 /tmp/nexus-wordcount.log >&2
    exit 1
  fi
  echo "WordCount OK :"
  hdfs dfs -cat /test-out/part-r-00000
'
