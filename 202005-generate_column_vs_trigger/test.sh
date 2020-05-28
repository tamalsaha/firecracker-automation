#!/bin/bash
set -e

docker-compose exec my_postgres12 /bin/bash -c  "psql -U postgres -d test -f /trigger_test/insert_data.sql && psql -U postgres -d test -f /trigger_test/update_data.sql"
