
create table foo (content jsonb);
insert into foo values (
        '{"props": {"a": 12, "fn": [10,20,30]}, "cd":"2017-11-22T15:20:34.326Z"}'
),(
        '{"props": {"a":5, "fn":["mix", 7.0, "2017-11-22", true, {"x":3.0} ]}}'
);

create table foo2 (version integer, content jsonb);
insert into foo2 values (
        1, '{"cd": "2017-11-22T15:20:34.326Z", "props": {"a": 12, "fn": [10, 20, 30]}}'
), (
        2, '{"cd": "2017-11-22T15:20:34.326Z", "props": {"a": "twelve", "fn": [10, 20, 30], "j": false}}'
);

create table foo3 (content jsonb);
insert into foo3 values (
        '{"props": {"a": 12, "fn": [{"$numberDecimal": "10"}, 20, {"$numberLong": "30"}]}, "cd": {"$date": 1511364034326}}'
), (
        '{"props": {"a": 5, "fn": ["mix", 7.0, {"$date": 1511308800000}, true, {"x": 3.0}]}}'
);
