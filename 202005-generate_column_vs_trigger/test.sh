#!/bin/bash
set -e

#docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -f /trigger_test/insert_data.sql && psql -U postgres -d test -f /trigger_test/update_data.sql"

docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -c \"DROP EXTENSION IF EXISTS trigger_test CASCADE;\" &&
 psql -U postgres -d test -c \"CREATE EXTENSION IF NOT EXISTS trigger_test CASCADE;\""


echo "-- Insert phase"
##pgbench
docker-compose exec my_postgres12 /bin/bash -c  " pgbench -U postgres -n -T 60 -c 1 -f /trigger_test/insert_data.sql test "
##meta-command \i
#docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -f /trigger_test/insert_data.sql"



echo "-- Update phase"
##pgbench
docker-compose exec my_postgres12 /bin/bash -c  " pgbench -U postgres -n -T 60 -c 1 -f /trigger_test/update_data.sql test"
##meta-command \i
#docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -f /trigger_test/update_data.sql"



