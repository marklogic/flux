Temporary README for internal usage.

Java 11 or higher is required.

To try this out locally:

1. Follow the instructions in the `CONTRIBUTING.md` file for setting up a local MarkLogic instance via Docker.
2. From the root of this repository, run `./gradlew buildTool`. This will build and copy the tool to `./nt`, which is
   gitignored.
3. Download https://jdbc.postgresql.org/download/postgresql-42.6.0.jar and copy it to `./nt/ext`
   (this is what a user looking to import from an RDBMS would do in production - i.e. add their JDBC driver to the
   ./ext directory of the extracted zip file).

At this point, you have NT (new tool, named TBD) installed at `./nt`. The contents of that
directory are what a user would see after downloading the NT zip file and extracting it. The commands below are
longer than they'd be in the real world - in the real world, the user is likely just running e.g.
`./bin/(name of tool)`.

You can run NT without any params to see a summary of the available commands:

    ./nt/bin/nt

You can also use the `help` command to see detailed usage for a specific command:

    ./nt/bin/nt help import_files

## Import from files

Run the following command to import 4 files from the given directory as new documents in MarkLogic:

```
./nt/bin/nt import_files --path "new-tool-cli/src/test/resources/mixed-files/*" \
  --clientUri "new-tool-user:password@localhost:8000" \
  --permissions "new-tool-role,read,new-tool-role,update" \
  --uriReplace ".*/mixed-files,'/test'"
```

You can also preview the data that was read via the `--preview` argument, and you can drop potentially verbose columns
via `--previewDrop`:

```
./nt/bin/nt import_files --path "new-tool-cli/src/test/resources/mixed-files/*" --preview 10 --previewDrop content
```

## Export rows to RDBMS

You can use `export_jdbc` to export rows selected via Optic to an RDBMS. The below example will find 15 rows in the
Medical/Authors view in MarkLogic and write them to a new table named `Author` in Postgres.

```
./nt/bin/nt export_jdbc --clientUri "new-tool-user:password@localhost:8003" \
  --jdbcUrl "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres" --jdbcDriver "org.postgresql.Driver" \
  --query "op.fromView ('Medical', 'Authors')" --table Author 
```

The Postgres instance running in Docker now has an "Author" table in the "postgres" database. You can manually inspect
this in Intellij by opening the "Database" view and creating a connection to
`jdbc:postgresql://localhost:5432/postgres`. You can find the username/password in the `docker-compose.yml` file in
the root of this repository.

## Import from RDBMS

Run the following to import the rows from the "Author" table, writing them as JSON documents:

```
./nt/bin/nt import_jdbc --jdbcUrl "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres" --jdbcDriver "org.postgresql.Driver" \
  --query "select * from author" \
  --clientUri "new-tool-user:password@localhost:8003" \
  --permissions "new-tool-role,read,new-tool-role,update" \
  --uriPrefix "/author/" --uriSuffix ".json" --collections jdbc-author
```

In qconsole, you can filter on "/author/" for the URI to see the 15 author documents.

## Copy between databases

You can copy documents from one database to another, including to the same database.

```
./nt/bin/nt copy --clientUri "new-tool-user:password@localhost:8003" \
  --collections "author" --categories "content,metadata" \
  --outputClientUri "new-tool-user:password@localhost:8000"
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

This will produce an assembly jar at `./new-tool-cli/build/libs/new-tool-cli-0.1-SNAPSHOT-all.jar`. 

You can now run any CLI command via spark-submit. This is an example of previewing an import of files - change the value
of `--path`, as an absolute path is needed, and of course change the value of `--master` to match that of your Spark
cluster:

```
$SPARK_HOME/bin/spark-submit --class com.marklogic.newtool.Submit \
--master spark://NYWHYC3G0W:7077 new-tool-cli/build/libs/new-tool-cli-0.1-SNAPSHOT-all.jar \
import_files --path /Users/rudin/workspace/new-tool/new-tool-cli/src/test/resources/mixed-files --preview 5 --previewDrop content
```

After spark-submit completes, you can refresh <http://localhost:8080> to see evidence of the completed application.

The assembly jar does not include the AWS SDK, as doing so would increase its size from about 8mb to close to 400mb. 
spark-submit allows for dependencies to be included via its `--packages` option. The following shows an example of 
previewing an import of files from an S3 bucket by including the AWS SDK as package dependencies (change the bucket name
to something you can access :

```
$SPARK_HOME/bin/spark-submit --class com.marklogic.newtool.Submit \
--packages org.apache.hadoop:hadoop-aws:3.3.6,org.apache.hadoop:hadoop-client:3.3.6 \
--master spark://NYWHYC3G0W:7077 new-tool-cli/build/libs/new-tool-cli-0.1-SNAPSHOT-all.jar \
import_files --path "s3a://changeme/*.*" --preview 10 --previewDrop content
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
3. For "Spark-submit options", enter `--class com.marklogic.newtool.Submit`.
4. For "Arguments", enter the CLI command all the args you would normally enter when using the CLI.

If your CLI command will be accessing S3, you most likely should not include `--s3AddCredentials`. The EMR EC2 instance
will already have access to the S3 buckets per the "EC2 instance profile" you configured while creating your cluster. 

Additionally, if your CLI command is accessing an S3 bucket in a region other than the one that EMR is running in, 
you can add `--s3Endpoint s3.us-east-1.amazon.com` as an argument, replacing "us-east-1" with the region that the
S3 buckets is in.

After adding your step, it will run. It typically takes about 30s for the step to run, and it may take a minute or so
for links to the logs to show up. It is very easy for a step to fail due to a security issue with accessing an S3 
bucket, whether it's the bucket containing the assembly jar or an S3 path in your CLI command. You may want to 
temporarily make the buckets you're accessing open to the public for reading.

