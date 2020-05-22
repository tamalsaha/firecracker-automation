select calls, 
round(total_time::numeric,2) as total_time,
round(min_time::numeric,2) as min_time,
round(max_time::numeric,2)as  max_time,
round(mean_time::numeric,2) as mean_time,
round(stddev_time::numeric,2) as stddev_time,
query::character varying(30) 
from pg_stat_statements where query like '%insert into square_%' order by 7 ;
