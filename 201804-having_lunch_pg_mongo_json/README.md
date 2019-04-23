# Having lunch with PostgreSQL, MongoDB and JSON

## Create the schema and SQL data

Create an empty database and run the script `schema_data.sql`. YMMV:

```
createdb mongodbpost
psql -f schema_data.sql mongodbpost
```

## Run the Java programs

First you will need to copy the `src/main/resources/db.properties.template` file into `src/main/resources/db.properties` and edit with your own parameters. Compile the program:

```
mvn clean package
```

and run either of the executables discussed on the blog post:

```
mvn exec:java -Dexec.mainClass=com.ongres.blog.lunch._01_richshape.Main
mvn exec:java -Dexec.mainClass=com.ongres.blog.lunch._02_polymorphism.Main
mvn exec:java -Dexec.mainClass=com.ongres.blog.lunch._03_versioned.Main
mvn exec:java -Dexec.mainClass=com.ongres.blog.lunch._04_asmongo.Main
```
