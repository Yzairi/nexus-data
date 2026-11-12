#!/usr/bin/env bash
set -euo pipefail

for attempt in $(seq 1 60); do
  if docker compose exec -T namenode hdfs dfs -ls / >/dev/null 2>&1 \
     && docker compose exec -T resourcemanager yarn node -list 2>/dev/null | grep -q 'RUNNING'; then
    echo "HDFS et YARN sont prêts."
    exit 0
  fi
  sleep 2
done

echo "Le cluster Hadoop n'est pas prêt après 120 secondes." >&2
docker compose ps >&2
exit 1
