#!/bin/bash
set -e

[ -z $(docker-compose ps -q ) ] && docker-compose up -d


docker-compose exec my_postgres12 /bin/bash -c  "apt-get update && apt-get install -y make gcc postgresql-server-dev-12 && \
 echo autovacuum=\'off\' >> /var/lib/postgresql/data/postgresql.conf  &&\
 echo shared_preload_libraries=\'pg_stat_statements\' >> /var/lib/postgresql/data/postgresql.conf &&\
 echo shared_buffers=\'256MB\' >> /var/lib/postgresql/data/postgresql.conf &&\
 echo max_wal_size=\'5GB\' >> /var/lib/postgresql/data/postgresql.conf  &&\
 echo checkpoint_timeout=\'30 min\' >> /var/lib/postgresql/data/postgresql.conf" 

docker-compose exec my_postgres12 /bin/bash -c  "cd /trigger_test && make && make install"

docker-compose restart my_postgres12


until docker-compose exec my_postgres12 /bin/bash -c "pg_isready -Upostgres -d test" ; do sleep 1; done

docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -c \"DROP EXTENSION IF EXISTS trigger_test CASCADE;\" &&
 psql -U postgres -d test -c \"CREATE EXTENSION IF NOT EXISTS trigger_test CASCADE;\""


