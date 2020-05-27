Test  for Generated Column vs trigger in PG 12
======================================

For reproduce the test see the following steps:

**Download :**
* `docker-compose.yml` and `pg.env` files 
* `trigger_test` folder 

**Start de container**
 ```
 docker-compose up -d
 ```
 
**Configure PostgreSQL for install the extension trigger_test**
 ```
 docker-compose exec my_postgres12 /bin/bash -c  "apt-get update && apt-get install -y make gcc postgresql-server-dev-12 && \
  echo shared_preload_libraries=\'pg_stat_statements\' >> /var/lib/postgresql/data/postgresql.conf &&\
  echo shared_buffers=\'256MB\' >> /var/lib/postgresql/data/postgresql.conf &&\
  echo max_wal_size=\'5GB\' >> /var/lib/postgresql/data/postgresql.conf  &&\
  echo checkpoint_timeout=\'30 min\' >> /var/lib/postgresql/data/postgresql.conf" 
 ```
 
**Restart the container for apply the change to install the extension trigger_test**
 ```
 docker-compose restart my_postgres12
 ```
 
**Apply the test**
 ```
 docker-compose exec my_postgres12 /bin/bash -c  "cd /trigger_test && make && make install && \
  psql -U postgres -d test -c \"DROP EXTENSION IF EXISTS trigger_test CASCADE;\" && 
  psql -U postgres -d test -c \"CREATE EXTENSION IF NOT EXISTS trigger_test CASCADE;\" && \
  psql -U postgres -d test -f /trigger_test/insert_data.sql && psql -U postgres -d test -f /trigger_test/update_data.sql" 
 ```

**Get the result**
 ```
 docker-compose exec my_postgres12 /bin/bash -c "echo '        INSERT RESULTS  ' && psql -U postgres -d test -f /trigger_test/insert_result.sql && \
 echo '        UPDATE RESULTS   ' && psql -U postgres -d test -f /trigger_test/update_result.sql"
 ```

Anthony R. Sotolongo León
asotolongo@ongres.com

