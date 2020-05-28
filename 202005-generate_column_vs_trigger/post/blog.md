
# Generate Columns vs Triggers en PostgreSQL 12 (INSERT/UPDATE)

  
  
  

## Introductions:

  

<div  style="text-align: justify">

Generated columns is a new feature from PostgreSQL 12, which consists columns whose value are derived or depend on other columns from same table and as long as these are not generated columns too. Having another small restrictions that you can be seen here. This feature can bring benefits directly over SELECT operations because the calculation was done before and stored.

</div>

  
  

The syntax to define this kind of column are the following:

  

```

...GENERATED ALWAYS AS (generation_expr) STORED...

```

**Example**

  

```

CREATE TABLE my_table (r int, area numeric GENERATED ALWAYS AS (r^2 * pi()) STORED );

```

  

<div  style="text-align: justify">

So far is only allowed clause STORED, this available stores the data calculated in the table, this activity consumes disk space. Previous 12 version, it can be achieve using trigger, The Generated Columns can bring benefit for a cleaner syntax too. Normally with trigger you can achieve more complex operations, although with Generated Column a way to do something complex is to use your own functions as long as these functions are INMUTABLE. In addition to clean syntax and the SELECT operations it would be good to analyze the performance difference between both options, this brings us to the objective of this blog. The following tests are to analyze the performance of INSERT/UPDATE operations using Generated Columns vs C and PL/pgsql triggers, and determine which have a better performance.

</div>

  

## Test:

<div  style="text-align: justify">

For this test an extension [trigger_test](https://gitlab.com/ongresinc/blog-posts-src/-/tree/generate_column_vs_trigger/202005-generate_column_vs_trigger/extension/trigger_test) was built, the extension has the structure of the tables and related triggers, in addition the autovacuum option was disabled to avoid this activity interfering in test results. The trigger_test extension uses the pg_stat_statements extension to log the INSERT/UPDATE operations times. The table’s structures are simple, they have a column to store the value of the square's side, and a second column to store the area of the square, this last one column is the “generated” column that depend from the first, like the example shown above.

</div>

  

**Tables:**

  
| Tables | Descriptions |
| ------------- |-------------|
| square_gc | Table with Generated Column |
| square_trgc | Table with C trigger |
| square_trgplpgsql | Table with PL/pgsql trigger|

  

**Server for tests:**

* EC2 t2.micro en AWS

* Ubuntu 18.04.4 LTS (Bionic Beaver)

* 1 GB RAM

* General Purpose Disk (SSD)

  
  

**PostgreSQL Configuration:**

* PostgreSQL 12.3

* Shared_buffer = 256 MB

* Work_mem = 4 MB

  

**Extension Compilation and installation :**

  

**Compilations**

  

```

make && make install

```

  
  

**Installation in the database**

  

```

CREATE EXTENSION trigger_test CASCADE;

```

The data for tables must be loaded from *insert_data.sql* and *update_data.sql* files in this order.

  

## Result:

  

**INSERT operations results:**

  

Using this query to get INSERT result:

```

SELECT calls,
round(total_time::numeric, 2) AS total_time,
round(min_time::numeric, 2) AS min_time,
round(max_time::numeric, 2) AS max_time,
round(mean_time::numeric, 2) AS mean_time,
round(stddev_time::numeric, 2) AS stddev_time,
query::character varying(30)
FROM pg_stat_statements WHERE query LIKE '%insert into square_%'
ORDER BY 7;

```

  
  

| Calls | Total_time | Min_time | Max_time | Mean_time | Stddev_time | Query |
| -------------:|-------------:| -------------:|------------:| -------------:|------------:|-------------|
| 40 | 410.81 | 9.81 | 11.70 | 10.27|0.47|insert into square_gc|
| 40 | 439.89 | 10.71 | 12.29 | 11.00|0.27|insert into square_trgc|
| 40 | 1137.87| 27.57 | 29.94 | 28.45|0.51|insert into square_trgplpgsql|

  

<div  style="text-align: justify">

A better performance can be seen in the table with the generated column, in case of mean_time it was 7% better than the table with C trigger and 74 % better than PL/pgpgsql.

</div>

  

**UPDATE operations results:**

  

Using this query to get UPDATE result:

```

SELECT calls,
round(total_time::numeric, 2) AS total_time,
round(min_time::numeric, 2) AS min_time,
round(max_time::numeric, 2) AS max_time,
round(mean_time::numeric, 2) AS mean_time,
round(stddev_time::numeric, 2) AS stddev_time,
query::character varying(30)
FROM pg_stat_statements WHERE query LIKE '%update square_%'
ORDER BY 7;

```

  
  

| Calls | Total_time | Min_time | Max_time | Mean_time | Stddev_time | Query |
| -------------:|-------------:| -------------:|------------:| -------------:|------------:|-------------|
| 40 | 63416.24 | 918.81 | 3819.08 | 1585.41|593.42|update square_gc|
| 40 | 74719.17 | 1153.74 | 3527.58 | 1867.98|474.68|update square_trgc|
| 40 | 97779.17| 2008.87 | 4822.75 | 2444.48|0.51|update square_trgplpgsql|

  

<div  style="text-align: justify">

A better performance can be seen in the table with the generated column, in case of mean_time it was 15% better than the table with C trigger and 35 % better than PL/pgpgsql.

</div>

  
  

## Conclusion:

  

<div  style="text-align: justify">

The response times for massive INSERT and UPDATE operations in the table with Generated Column can have better performance than the other tables with triggers,even better than triggers in C, then it will confirm better performance for these activities in addition to having a cleaner syntax. This new PostgreSQL 12 feature shows us how PostgreSQL is keeping up the development and the possibility that in the future more benefits will be obtained through this feature.

</div>

  

## Download test docker-compose:

You can check this case study downloading the following [docker-compose file](https://gitlab.com/ongresinc/blog-posts-src/-/tree/generate_column_vs_trigger/202005-generate_column_vs_trigger/docker-compose)