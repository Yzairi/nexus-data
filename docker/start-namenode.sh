#!/usr/bin/env bash
set -euo pipefail

if [[ ! -f /tmp/nexus-hdfs/name/current/VERSION ]]; then
  hdfs namenode -format -force -nonInteractive
fi

exec hdfs namenode
