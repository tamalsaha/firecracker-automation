#!/bin/bash
set -e


sudo apt-get update && sudo apt-get install -y make gcc postgresql-server-dev-12

cd /trigger_test && make && make install

echo shared_preload_libraries=\'pg_stat_statements\' >> /var/lib/postgresql/data/postgresql.conf 
#echo shared_buffers=\'256MB\' >> /var/lib/postgresql/data/postgresql.conf 

