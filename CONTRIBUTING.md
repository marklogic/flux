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
a password of "postgres". For logging into Postgres itself, use "postgres" as the username and password. You can then
register a server that connects to the "postgres" server.

## Publishing the 2.2-SNAPSHOT Spark connector

This project will likely depend on a snapshot of our Spark connector until its 1.0 release. To set this up, complete
the following steps:

1. Clone [our Spark connector](https://github.com/marklogic/marklogic-spark-connector) if you have not already.
2. From that repository's project directory, change to the `develop` branch.
3. Run `./gradlew publishToMavenLocal`.

You will now be able to import this repository's project into Intellij and run the tests.

## Running the tests

*You must use Java 11* or higher to run any Gradle tasks in this project, due to the inclusion of the Sonar Gradle 
plugin. The tool itself only requires Java 8, but building requires Java 11.

You can run the tests once you've followed the instructions above for loading the DVD rental dataset into Postgres and
publishing a local snapshot of our Spark connector. Then just run:

    ./gradlew clean test

## Generating code quality reports with SonarQube

In order to use SonarQube, you must have used Docker to run this project's `docker-compose.yml` file, and you must
have the services in that file running.

To configure the SonarQube service, perform the following steps:

1. Go to http://localhost:9000 .
2. Login as admin/admin. SonarQube will ask you to change this password; you can choose whatever you want ("password" works).
3. Click on "Create project manually".
4. Enter "spark-etl" for the Project Name; use that as the Project Key too.
5. Enter "main" as the main branch name.
6. Click on "Next".
7. Click on "Use the global setting" and then "Create project".
8. On the "Analysis Method" page, click on "Locally".
9. In the "Provide a token" panel, click on "Generate". Copy the token.
10. Add `systemProp.sonar.token=your token pasted here` to `gradle-local.properties` in the root of your project, creating
    that file if it does not exist yet.

To run SonarQube, run the following Gradle tasks, which will run all the tests with code coverage and then generate
a quality report with SonarQube:

    ./gradlew test sonar

If you do not add `systemProp.sonar.token` to your `gradle-local.properties` file, you can specify the token via the
following:

    ./gradlew test sonar -Dsonar.token=paste your token here

When that completes, you will see a line like this near the end of the logging:

    ANALYSIS SUCCESSFUL, you can find the results at: http://localhost:9000/dashboard?id=spark-etl

Click on that link. If it's the first time you've run the report, you'll see all issues. If you've run the report
before, then SonarQube will show "New Code" by default. That's handy, as you can use that to quickly see any issues
you've introduced on the feature branch you're working on. You can then click on "Overall Code" to see all issues.

Note that if you only need results on code smells and vulnerabilities, you can repeatedly run `./gradlew sonar`
without having to re-run the tests.

## Testing the documentation locally

The docs for this project are stored in the `./docs` directory as a set of Markdown files. These are published via
[GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/about-github-pages) using the
configuration found under "Settings / Pages" in this repository.

You can build and test the docs locally by
[following these GitHub instructions](https://docs.github.com/en/pages/setting-up-a-github-pages-site-with-jekyll/testing-your-github-pages-site-locally-with-jekyll),
though you don't need to perform all of those steps since some of the files generated by doing so are already in the
`./docs` directory. You just need to do the following:

1. Install the latest Ruby (rbenv works well for this).
2. Install Jekyll.
3. Go to the docs directory - `cd ./docs` .
4. Run `bundle install` (this may not be necessary due to Gemfile.lock being in version control).
5. Run `bundle exec jekyll serve`.

You can then go to http://localhost:4000 to view the docs. 
