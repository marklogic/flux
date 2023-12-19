Temporary README for internal usage.

Java 11 or higher is required.

To try this out locally:

1. Follow the instructions in the `CONTRIBUTING.md` file for setting up a local MarkLogic instance via Docker.
2. From the root of this repository, run `cd new-tool-cli`.
3. Run `../gradlew installDist`
4. Download https://jdbc.postgresql.org/download/postgresql-42.6.0.jar and copy it to `./build/install/new-tool-cli/ext`
(this is what a user looking to import from an RDBMS would do in production - i.e. add their JDBC driver to the
./ext directory of the extracted zip file).

At this point, you have NT (new tool, named TBD) installed at ./new-tool-cli/build/install/new-tool-cli. The contents of that 
directory are what a user would see after downloading the NT zip file and extracting it. The commands below are thus
much longer than they'd be in the real world - in the real world, the user is likely just running e.g. 
`./bin/(name of tool)`.

You can run NT without any params to get usage:

    ./build/install/new-tool-cli/bin/new-tool-cli

## Export to RDBMS

You can use `export_jdbc` to export rows selected via Optic to an RDBMS. The below example will find 15 rows in the 
Medical/Authors view in MarkLogic and write them to a new table named `Author` in Postgres.

```
./build/install/new-tool-cli/bin/new-tool-cli export_jdbc --jdbcUrl "jdbc:postgresql://localhost/postgres" --jdbcTable Author --jdbcDriver "org.postgresql.Driver" --jdbcUser "postgres" --jdbcPassword "postgres" --query "op.fromView ('Medical', 'Authors')" 
```

The Postgres instance running in Docker now has an "Author" table in the "postgres" database. You can manually inspect
this in Intellij by opening the "Database" view and creating a connection to 
`jdbc:postgresql://localhost:5432/postgres`. You can find the username/password in the `docker-compose.yml` file in 
the root of this repository.

## Import from RDBMS

Run the following to import the rows from the "Author" table, writing them as JSON documents:

```
./build/install/new-tool-cli/bin/new-tool-cli import_jdbc --jdbcUrl "jdbc:postgresql://localhost/postgres" --jdbcTable Author --jdbcDriver "org.postgresql.Driver" --jdbcUser "postgres" --jdbcPassword "postgres" --uriPrefix "/author/" --uriSuffix ".json" --collections jdbc-author
```

In qconsole, you can filter on "/author/" for the URI to see the 15 author documents.


