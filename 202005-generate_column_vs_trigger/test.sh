#!/bin/bash
set -e

#docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -f /trigger_test/insert_data.sql && psql -U postgres -d test -f /trigger_test/update_data.sql"

echo "-- Insert phase"
docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -f /trigger_test/insert_data.sql"


for table in square_gc square_trgc square_trgc_immutable square_trgplpgsql square_trgplpgsql_immutable
do
        maxi=$(docker-compose exec my_postgres12 /bin/bash -c  "psql -Antq -U postgres -d test -c \"SELECT max(i) from ${table}\" ")
        echo "-- Updates on ${table}"
        for it in $(seq 1 20)
        do
                docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -c \"update ${table} set ts = now() WHERE i = round(random()*${maxi}-1);\""
        done     
done

