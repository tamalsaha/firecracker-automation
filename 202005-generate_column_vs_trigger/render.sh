#!/bin/bash
set -e

echo "# Insert Results"

docker-compose exec my_postgres12 /bin/bash -c "psql -U postgres -d test -f /trigger_test/insert_result.sql"

echo "# Update Results"
docker-compose exec my_postgres12 /bin/bash -c "psql -U postgres -d test -f /trigger_test/update_result.sql"



