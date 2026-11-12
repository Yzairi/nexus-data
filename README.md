# Nexus Hadoop — Docker + Java MapReduce

Ce dossier contient un cluster Hadoop 3.3.6 reproductible et deux pipelines Java MapReduce.

## Traitements

1. **Activité / DAU**
   - jointure télémétrie + référentiel joueurs ;
   - exclusion des joueurs absents de `players` ;
   - déduplication par joueur et jour ;
   - comptage des joueurs actifs distincts par date UTC.

2. **Incidents et abandons**
   - lecture des schémas de télémétrie v1 et v2 ;
   - lecture des deux formats de logs ;
   - jointure par `session_id` ;
   - agrégation globale, par version et par région.

Les doublons strictement identiques n'affectent pas ces calculs : le DAU travaille sur des couples joueur/jour distincts et l'analyse technique sur des indicateurs booléens par session.

## Démonstration rapide

Prérequis : Docker Desktop, Maven et Python 3.

```bash
cd hadoop-nexus
chmod +x scripts/*.sh
./scripts/run-demo.sh
```

La démonstration crée automatiquement un échantillon de la partition du 21 août : **10 logs maximum**, leurs événements de télémétrie et les joueurs concernés. Elle compile le JAR, démarre HDFS/YARN, ingère l'échantillon et exécute les deux pipelines.

Interfaces :

- NameNode : http://localhost:9870
- ResourceManager : http://localhost:8088
- JobHistory : http://localhost:19888

Arrêt du cluster :

```bash
docker compose down
```

Suppression volontaire des petits volumes de démonstration :

```bash
docker compose down -v
```

## Test Hadoop WordCount

Après le démarrage du cluster :

```bash
./scripts/test-cluster.sh
```

Le résultat attendu contient notamment :

```text
hadoop  2
docker  1
nexus   1
```

## Commandes manuelles

Compilation :

```bash
mvn -Dmaven.repo.local=.m2/repository clean test package
```

Création de l'échantillon :

```bash
python3 scripts/create_sample.py \
  --source ../data/source \
  --date 2026-08-21 \
  --max-logs 10 \
  --output data/sample
```

Lancement :

```bash
docker compose up -d
./scripts/wait-cluster.sh
```

Vérification HDFS/YARN :

```bash
docker compose exec namenode hdfs dfs -ls /
docker compose exec resourcemanager yarn node -list
```

## Téléchargement sans les fichiers `index.html`

À utiliser seulement si les données ne sont pas déjà disponibles :

```bash
cd hadoop-nexus/data
BASE='https://gdau8rrtb2czlzgbh4uocjqb.51.75.28.54.sslip.io/'
OPTS='-r -np -nH -c --user student --ask-password -R index.html*'

for d in 2026-08-{15..21}; do
  wget $OPTS "$BASE/telemetry/dt=$d/" "$BASE/logs/dt=$d/"
done

wget $OPTS "$BASE/manifest.json" "$BASE/references/" "$BASE/mongo/"
```

Le mot de passe est saisi uniquement à l'invite de `wget --ask-password` et ne doit pas être enregistré dans le dépôt.

## Exécution sur davantage de données

Positionner les sources dans HDFS :

```text
/nexus/raw/telemetry/
/nexus/raw/logs/
/nexus/raw/players/
```

Puis lancer :

```bash
yarn jar target/nexus-hadoop.jar activity \
  /nexus/raw/telemetry /nexus/raw/players \
  /nexus/processed/activity-days /nexus/analytics/dau

yarn jar target/nexus-hadoop.jar technical \
  /nexus/raw/telemetry /nexus/raw/logs \
  /nexus/processed/technical-sessions /nexus/analytics/technical
```

Les dossiers de sortie doivent être absents avant le lancement. Les compteurs Hadoop exposent les lignes rejetées, joueurs inconnus, sessions jointes, incidents et abandons.

## Choix de compatibilité

Le code source est compilé avec le JDK installé par Maven et produit du bytecode Java 8 afin de fonctionner avec le JRE 8 fourni par l'image officielle Hadoop 3.3.6. Sur un Mac Apple Silicon, Docker exécute cette image officielle `linux/amd64` par émulation. Les traitements utilisent uniquement l'API MapReduce Hadoop 3.3.6.
