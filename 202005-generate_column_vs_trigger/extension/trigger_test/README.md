trigger_test  extension
======================================



Trigger_test extension is for test the performance between generared columns vs trigger in c and trigger plpgsql

**REQUIREMENTS**
* require PG12+ and  pg_stat_statements extension
* require dev library for postgres, in ubuntu called apt-get install postgresql-server-dev-12

**INSTALL**

```
make
make install
```

**CREATE EXTENSION**
```
CREATE EXTENSION trigger_test CASCADE;
```

**LOAD DATA**
The data to test is locate in the insert_data.sql and update_data.sql files, please load these files, first insert_data.sql and then update_data.sql



 **ANALYZE THE INSERT RESULT**

```
select calls, 
round(total_time::numeric,2) as total_time,
round(min_time::numeric,2) as min_time,
round(max_time::numeric,2)as  max_time,
round(mean_time::numeric,2) as mean_time,
round(stddev_time::numeric,2) as stddev_time,
query::character varying(30) 
from pg_stat_statements where query like '%insert into square_%' order by 7 ;
```

**ANALYZE THE UPDATE RESULT**

```
select calls, 
round(total_time::numeric,2) as total_time,
round(min_time::numeric,2) as min_time,
round(max_time::numeric,2)as  max_time,
round(mean_time::numeric,2) as mean_time,
round(stddev_time::numeric,2) as stddev_time,
query::character varying(30) 
from pg_stat_statements where query like '%update square_%' order by 7 ;
```


**DROP EXTENSION**
```
DROP EXTENSION trigger_test CASCADE;
```
Anthony R. Sotolongo León
asotolongo@ongres.com

