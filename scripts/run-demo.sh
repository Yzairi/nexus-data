#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$PROJECT_DIR"

mvn -q -Dmaven.repo.local=.m2/repository clean test package
python3 scripts/create_sample.py --source ../data/source --max-logs 10 --output data/sample

docker compose up -d
scripts/wait-cluster.sh

docker compose exec -T namenode bash -lc '
  hdfs dfs -rm -r -f /nexus/raw/sample /nexus/processed/sample /nexus/analytics/sample >/dev/null 2>&1 || true
  hdfs dfs -mkdir -p /nexus/raw/sample /nexus/processed/sample /nexus/analytics/sample
  hdfs dfs -put /workspace/data/sample/telemetry /nexus/raw/sample/telemetry
  hdfs dfs -put /workspace/data/sample/logs /nexus/raw/sample/logs
  hdfs dfs -put /workspace/data/sample/players /nexus/raw/sample/players

  if ! yarn jar /workspace/target/nexus-hadoop.jar activity \
    /nexus/raw/sample/telemetry \
    /nexus/raw/sample/players \
    /nexus/processed/sample/activity-days \
    /nexus/analytics/sample/dau >/tmp/nexus-activity.log 2>&1; then
    tail -n 40 /tmp/nexus-activity.log >&2
    exit 1
  fi

  if ! yarn jar /workspace/target/nexus-hadoop.jar technical \
    /nexus/raw/sample/telemetry \
    /nexus/raw/sample/logs \
    /nexus/processed/sample/technical-sessions \
    /nexus/analytics/sample/technical >/tmp/nexus-technical.log 2>&1; then
    tail -n 40 /tmp/nexus-technical.log >&2
    exit 1
  fi

  echo "DAU :"
  hdfs dfs -cat /nexus/analytics/sample/dau/part-r-*
  echo "Indicateurs techniques :"
  hdfs dfs -cat /nexus/analytics/sample/technical/part-r-*
'
