To contribute to this project, complete these steps to setup a MarkLogic instance via Docker with a test 
application installed:

1. Clone this repository if you have not already.
2. From the root directory of the project, run `docker-compose up -d --build`.
3. Wait 10 to 20 seconds and verify that <http://localhost:8001> shows the MarkLogic admin screen before proceeding.
4. Run `./gradlew -i mlDeploy` to deploy this project's test application (note that Java 11 or higher is required).

Some of the tests depend on the Postgres instance deployed via Docker. Follow these steps to load a sample dataset
into it:

1. Go to [this Postgres tutorial](https://www.postgresqltutorial.com/postgresql-getting-started/postgresql-sample-database/).
2. Scroll down to the section titled "Download the PostgreSQL sample database". Follow the instructions there for 
downloading the `dvdrental.zip` and extracting it to produce a file named `dvdrental.tar` (one option is to use Java - 
`jar -xvf dvdrental.zip`).
3. Copy `dvdrental.tar` to `./docker/postgres/dvdrental.tar` in this project.

Once you have the `dvdrental.tar` file in place, run these commands to load it into Postgres:

```
docker exec -it new_tool-postgres-1 psql -U postgres -c "CREATE DATABASE dvdrental"
docker exec -it new_tool-postgres-1 pg_restore -U postgres -d dvdrental /opt/dvdrental.tar
```

The Docker file includes a pgadmin instance which can be accessed at <http://localhost:15432/>. 
If you wish to login to this, do so with "postgres@pgadmin.com" and 
a password of "postgres". For logging into Postgres itself, use "postgres" as the username and password.

## Publishing the 2.2-SNAPSHOT Spark connector

This project will likely depend on a snapshot of our Spark connector until its 1.0 release. To set this up, complete
the following steps:

1. Clone [our Spark connector](https://github.com/marklogic/marklogic-spark-connector) if you have not already.
2. From that repository's project directory, change to the `develop` branch.
3. Run `./gradlew publishToMavenLocal`.

You will now be able to import this repository's project into Intellij and run the tests.

## Running the tests

You can run the tests once you've followed the instructions above for loading the DVD rental dataset into Postgres and
publishing a local snapshot of our Spark connector. Then just run:

    ./gradlew clean test
