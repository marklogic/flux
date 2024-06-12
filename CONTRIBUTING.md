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
docker exec -it flux-postgres-1 psql -U postgres -c "CREATE DATABASE dvdrental"
docker exec -it flux-postgres-1 pg_restore -U postgres -d dvdrental /opt/dvdrental.tar
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

## Building the distribution locally

If you would like to test our the Flux distribution - as either a tar or zip - perform the following steps:

1. Run either `./gradlew distTar` or `./gradlew distZip`.
2. Move the file created at `./flux-cli/build/distributions` to a desired location.
3. Extract the file. 
4. `cd` into the extracted directory.

You can now run `./bin/flux` to test out various commands. 

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

## Examining error reports

The following commands show examples of how the tool reports errors. One gap is that when a batch fails, the contents
of the batch are not yet logged. This is an area of research as while the URIs can easily be included in the error
reporting, they are not necessarily helpful. Support for trying each document individually may be added in the future.

You can test an invalid command name:

    ./flux/bin/flux not_a_real_command

You can forget a required argument:

    ./flux/bin/flux import-files

You can cause an error from Spark:

    ./flux/bin/flux import-files --path invalid-path

You can cause a failure with MarkLogic that caused the command to stop:

```
./flux/bin/flux import-files --path "flux-cli/src/test/resources/mixed-files/*" \
  --connection-string "flux-user:password@localhost:8000" \
  --repartition 1 \
  --abort-on-write-failure \
  --permissions "invalid-role,read,flux-role,update" \
  --uri-replace ".*/mixed-files,'/test'"
```

You can cause a failure and ask to see the full stacktrace (often noisy and not helpful):

```
./flux/bin/flux import-files --path "flux-cli/src/test/resources/mixed-files/*" \
  --connection-string "flux-user:password@localhost:8000" \
  --repartition 1 \
  --permissions "invalid-role,read,flux-role,update" \
  --uri-replace ".*/mixed-files,'/test'" \
  --abort-on-write-failure \
  --stacktrace
```

You can cause a failure and tell the command to keep executing by not including `--abort-on-write-failure`:

```
./flux/bin/flux import-files --path "flux-cli/src/test/resources/mixed-files/*" \
  --connection-string "flux-user:password@localhost:8000" \
  --permissions "invalid-role,read,flux-role,update" \
  --uri-replace ".*/mixed-files,'/test'"
```

## Testing with a load balancer

The `docker-compose.yml` file includes an instance of a 
[Caddy load balancer](https://caddyserver.com/docs/caddyfile/directives/reverse_proxy). This is useful for any kind 
of performance testing, as you typically want Flux (and our Spark connector) to connect to a load balancer that can 
both distribute load and handle retrying failed connections. 

The `./caddy/config/Caddyfile` configuration file has some default config in it for communicating with a 3-node cluster
owned by the performance team. Feel free to adjust this config locally as needed. 

Example of using the existing config to copy from port 8015 to port 8016 in the performance cluster:

```
./flux/bin/flux copy --connection-string "admin:admin@localhost:8006" \
  --collections "address_small" \
  --batch-size 500 \
  --limit 10000 \
  --categories content,metadata \
  --output-connection-string "admin:admin@localhost:8007" \
  --output-thread-count 3 --partitions-per-forest 1 --output-batch-size 200
```

## Testing against a separate Spark cluster

This section describes how to test the ETL tool against a separate Spark cluster instead of having the tool stand up
its own temporary Spark environment.

To begin, install Spark via [sdkman](https://sdkman.io/sdks#spark), unless you already have a Spark cluster ready.
Verify that ports 8080 and 7077 are available as well, as Spark will attempt to listen on both.

Set `SPARK_HOME` to the location of Spark - e.g. `/Users/myname/.sdkman/candidates/spark/current`.

Next, start a Spark master node:

    cd $SPARK_HOME/bin
    ./start-master.sh

You will need the address at which the Spark master node can be reached. To find it, open the log file that Spark
created under `$SPARK_HOME/logs` - it will have the word "master" in its filename - and look for text like the following
near the end of the log file:

    INFO Master: Starting Spark master at spark://NYWHYC3G0W:7077

Now start a Spark worker node by referencing that address:

    ./start-worker.sh spark://NYWHYC3G0W:7077

To verify that Spark is running correctly, go to <http://localhost:8080>. You should see the Spark Master web interface,
along with a link to the worker that you started. You are now able to run tests against this Spark cluster.

### Testing with spark-submit

Spark's [spark-submit](https://spark.apache.org/docs/latest/submitting-applications.html) program allows for a Spark
program to be run on a separate (and possibly remote) Spark cluster. Now that you have a separate Spark cluster running
per the above instructions, you can test each CLI command by running it via spark-submit.

First, you must build an assembly jar that contains the required CLI functionality in a single jar file (this uses
the [Gradle shadow jar plugin](https://imperceptiblethoughts.com/shadow/); "assembly jar", "shadow jar", and "uber jar"
are all synonyms):

    ./gradlew shadowJar

This will produce an assembly jar at `./flux-cli/build/libs/flux-cli-0.2.0-all.jar`.

You can now run any CLI command via spark-submit. This is an example of previewing an import of files - change the value
of `--path`, as an absolute path is needed, and of course change the value of `--master` to match that of your Spark
cluster:

```
$SPARK_HOME/bin/spark-submit --class com.marklogic.flux.cli.Submit \
--master spark://NYWHYC3G0W:7077 flux-cli/build/libs/flux-cli-0.2.0-all.jar \
import-files --path /Users/rudin/workspace/flux/flux-cli/src/test/resources/mixed-files --preview 5 --preview-drop content
```

After spark-submit completes, you can refresh <http://localhost:8080> to see evidence of the completed application.

The assembly jar does not include the AWS SDK, as doing so would increase its size from about 8mb to close to 400mb.
spark-submit allows for dependencies to be included via its `--packages` option. The following shows an example of
previewing an import of files from an S3 bucket by including the AWS SDK as package dependencies (change the bucket name
to something you can access :

```
$SPARK_HOME/bin/spark-submit --class com.marklogic.flux.cli.Submit \
--packages org.apache.hadoop:hadoop-aws:3.3.6,org.apache.hadoop:hadoop-client:3.3.6 \
--master spark://NYWHYC3G0W:7077 flux-cli/build/libs/flux-cli-0.1-SNAPSHOT-all.jar \
import-files --path "s3a://changeme/*.*" --preview 10 --preview-drop content
```

### Testing with AWS EMR

[AWS EMR](https://docs.aws.amazon.com/emr/) supports running spark-submit in AWS.

This is not intended to be a reference on how to use EMR. Instead, the following are recommended while creating a
cluster:

1. Before creating a cluster, build the assembly jar as described above and upload it to an S3 bucket in the same
   AWS region as where you'll be creating an EMR cluster.
2. In the "Create a cluster" form, keep "Publish cluster-specific logs to Amazon S3" checked, as otherwise you won't
   have access to logs written by your EMR step.
3. In the "IAM Roles" section while creating a cluster, it is recommended to create a new EMR service role and
   a new EC2 instance profile for EMR.
4. For the EC2 instance profile, ensure that it has access to any S3 buckets you will be using, including the bucket
   that you uploaded the assembly jar to.

Once your cluster is created, you'll add a "Step" in order to run spark-submit:

1. Choose "Spark application" for the type of job.
2. For "JAR location", select the assembly jar that you uploaded to S3.
3. For "Spark-submit options", enter `--class com.marklogic.flux.cli.Submit`.
4. For "Arguments", enter the CLI command all the args you would normally enter when using the CLI.

If your CLI command will be accessing S3, you most likely should not include `--s3-add-credentials`. The EMR EC2 instance
will already have access to the S3 buckets per the "EC2 instance profile" you configured while creating your cluster.

Additionally, if your CLI command is accessing an S3 bucket in a region other than the one that EMR is running in,
you can add `--s3-endpoint s3.us-east-1.amazon.com` as an argument, replacing "us-east-1" with the region that the
S3 buckets is in.

After adding your step, it will run. It typically takes about 30s for the step to run, and it may take a minute or so
for links to the logs to show up. It is very easy for a step to fail due to a security issue with accessing an S3
bucket, whether it's the bucket containing the assembly jar or an S3 path in your CLI command. You may want to
temporarily make the buckets you're accessing open to the public for reading.
