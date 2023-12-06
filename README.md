To try this out locally:

1. Install/start Docker if you don't already have it installed/started.
2. Stop any MarkLogic instance you have running on your localhost.
3. Run `docker-compose up -d --build`. This will start a Docker service with both MarkLogic and Postgres running.
4. Run `gradlew -i mlDeploy`
5. `cd new-tool-cli`
6. Run `../gradlew installDist`
7. Download https://jdbc.postgresql.org/download/postgresql-42.6.0.jar and copy it to `./build/install/new-tool-cli/ext`.

At this point, you have NT (new tool) installed at ./new-tool-cli/build/install/new-tool-cli. The contents of that 
directory are what a user would see after downloading an NT zip file and extracting it. The commands below are thus
much longer than they'd be in the real world - in the real world, the user is likely just running e.g. 
`./bin/(name of tool)`.

You can run NT without any params to get usage:

    ./build/install/new-tool-cli/bin/new-tool-cli

## Import CSV

Try the following to import 20k rows as JSON into the new-tool-content database:

```
./build/install/new-tool-cli/bin/new-tool-cli import_files --format csv --path rows20k.csv -R:header=true --uri-template "/test/{TEST_FIELD}.json" --collections csv-test
```

For local testing, this should complete in under 10 seconds or so. You'll then see 20k docs in the `csv-test`
collection (they all have some default permissions that are set in the tool; this wouldn't be the case in the real
world).

## Import CSV from S3

Reading CSV files from S3 is supported - e.g. 

```
./build/install/new-tool-cli/bin/new-tool-cli import_files --format csv --path s3a://rudin-test-bucket/rows20k.csv -R:header=true --collections s3test
```

For the above to work, you'll need to configure your AWS credentials - such as in your `~/.aws/credentials` file - 
so that the bundled AWS SDK can find them. And then of course the path must point to a file or directory in S3 that 
you can access. 


## Export to RDBMS

You can use `export_jdbc` to export rows selected via Optic to an RDBMS. The below example will find 15 rows in the 
Medical/Authors view in MarkLogic and write them to a new table named `Author` in Postgres.

```
./build/install/new-tool-cli/bin/new-tool-cli export_jdbc --jdbc-url "jdbc:postgresql://localhost/postgres" --jdbc-table Author --jdbc-driver "org.postgresql.Driver" --jdbc-user "postgres" --jdbc-password "postgres" --query "op.fromView ('Medical', 'Authors')" 
```

The Postgres instance running in Docker now has an "Author" table in the "postgres" database. You can manually inspect
this in Intellij by opening the "Database" view and creating a connection to 
`jdbc:postgresql://localhost:5432/postgres`. You can find the username/password in the `docker-compose.yml` file in 
the root of this repository.

## Import from RDBMS

Run the following to import the rows from the "Author" table, writing them as JSON documents:

```
./build/install/new-tool-cli/bin/new-tool-cli import_jdbc --jdbc-url "jdbc:postgresql://localhost/postgres" --jdbc-table Author --jdbc-driver "org.postgresql.Driver" --jdbc-user "postgres" --jdbc-password "postgres" --uri-prefix "/author/" --uri-suffix ".json" --collections jdbc-author
```

In qconsole, you can filter on "/author/" for the URI to see the 15 author documents.


## Trying out the DVD rental dataset

Follow the same instructions as for 
[this Spark example project](https://github.com/marklogic/marklogic-spark-connector/tree/master/examples/entity-aggregation).
In particular, you need to download the `dvdrental.zip` file and extract it to `./docker/postgres/dvdrental.tar` 
in this project so that the Postgres instance created via docker-compose can see it. 

The Docker commands are slightly different due to the container names:

```
docker exec -it marklogic_new_tool-postgres-1 psql -U postgres -c "CREATE DATABASE dvdrental"
docker exec -it marklogic_new_tool-postgres-1 pg_restore -U postgres -d dvdrental /opt/dvdrental.tar
```

For logging into pgadmin, use postgres@pgadmin.com and "postgres". For logging into postgres, use postgres/postgres.

## Reprocess

You can reprocess data, like in Corb.

```
./build/install/new-tool-cli/bin/new-tool-cli reprocess --read-javascript "cts.uris(null, null, cts.collectionQuery('test-data'))" --write-javascript "var URI; console.log('URI', URI)"
```

## Running the tests

You can run the tests once you've followed the instructions above for loading the DVD rental dataset into the Postgres
instance created by docker-compose. Then just run:

    ./gradlew clean test

