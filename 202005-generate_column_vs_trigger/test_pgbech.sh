#!/bin/bash
set -e

#docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -f /trigger_test/insert_data.sql && psql -U postgres -d test -f /trigger_test/update_data.sql"

echo "-- Insert phase"
docker-compose exec my_postgres12 /bin/bash -c  " pgbench -U postgres -n -T 60 -c 1 -f /trigger_test/insert_data.sql test "


echo "-- Update phase"
docker-compose exec my_postgres12 /bin/bash -c  " pgbench -U postgres -n -T 180 -c 1 -f /trigger_test/update_data.sql test"


