# Docker Lab for Generated Column vs trigger in PG 12



For reproduce the test see the following steps:

```
curl https://gitlab.com/ongresinc/blog-posts-src/-/archive/generate_column_vs_trigger/blog-posts-src-generate_column_vs_trigger.tar.gz\?path\=202005-generate_column_vs_trigger --output blog.tar.gz
tar xzf blog.tar.gz
cd blog-posts-src-generate_column_vs_trigger-202005-generate_column_vs_trigger
```

For improving readiness and avoid user's valuable time, we coded a few scripts that will setup the environment for reproducing the benchmarks in a local environment.

```
./clean.sh
./init.sh
./test.sh
./render.sh
```

Observe that the `./clean.sh` script is at the beginning, in order to avoid dirty previous runs. Use the same script for cleaning up your environment before you leave!

The `init.sh` phase could take some time and should setup all the necessary pieces for running the test.

## `trigger_test` extension

This laboratory, contains all the functions and related tables in the `trigger_test` folder, which can be compiled and added to the postgres instance. For reference, see `init.sh`

 
## Results calculation

### INSERT operations results
 
```sql
SELECT   round(mean_time::numeric, 2) AS mean_time,
  query::character varying(40)
FROM  pg_stat_statements WHERE query LIKE '%insert into tab_%'
ORDER BY 7;
```

### UPDATE operations

```sql
SELECT     round(mean_time::numeric, 2) AS mean_time,
   query::character varying(40)
FROM  pg_stat_statements WHERE query LIKE '%update tab_%'
ORDER BY 7;
```


Anthony R. Sotolongo León
asotolongo@ongres.com
/
Emanuel Calvo
emanuel@ongres.com



