# Test court — Hadoop Nexus

Date : 12 novembre 2026

- Cluster Docker : **5 services actifs**, HDFS et YARN opérationnels.
- WordCount : **OK** (`hadoop=2`, `docker=1`, `nexus=1`).
- Build Java : **OK**, 2 tests unitaires réussis.
- Échantillon : **200 événements, 8 joueurs, 10 logs**.
- Activité : **DAU = 4** le 21 août 2026.
- Technique : **5 sessions**, 40 % avec incident, 20 % abandonnées.
- Abandon après incident : **50 %** sur ce petit échantillon.

Commande de reproduction : `cd hadoop-nexus && scripts/run-demo.sh`
