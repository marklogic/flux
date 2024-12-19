MarkLogic Flux is a single extensible application for all of your data movement use cases with MarkLogic.
Flux supports importing, exporting, copying, and reprocessing data via a simple command-line interface.
Flux can also be easily embedded in your own application to support any flow of data to and from MarkLogic.

With Flux, you can automate common data movement use cases including:

- Importing rows from an RDBMS.
- Importing JSON, XML, CSV, Parquet and other file types from a local filesystem or S3.
- Implementing a data pipeline for a [RAG solution with MarkLogic](https://www.progress.com/marklogic/solutions/generative-ai).
- Copying data from one MarkLogic database to another database.
- Reprocessing data in MarkLogic via custom code.
- Exporting data to an RDBMS, a local filesystem, or S3.

Flux leverages the [MarkLogic Spark connector](https://github.com/marklogic/marklogic-spark-connector) and
[Apache Spark](https://spark.apache.org/) to support a wide variety of data sources and data formats. Current users of 
[MarkLogic Content Pump](https://developer.marklogic.com/products/mlcp/) and
[CoRB 2](https://developer.marklogic.com/code/corb/) can easily switch to Flux to support their current data movement needs and also start leveraging
the new data sources and formats that may have required custom development in the past to integrate with 
MarkLogic.

You can download the latest release of Flux from [the releases page](https://github.com/marklogic/flux/releases).

For more information, please see [the user guide](https://marklogic.github.io/flux/).

## Support

Flux is maintained by Progress MarkLogic and distributed under the 
[Apache 2.0 license](https://github.com/marklogic/flux/blob/LICENSE). 
Progress MarkLogic provides technical support for [all releases of Flux](https://github.com/marklogic/flux/releases) 
to licensed customers under the terms outlined in the [Support Handbook](http://www.marklogic.com/files/Mark_Logic_Support_Handbook.pdf). 
For more information or to sign up for support, visit [help.marklogic.com](http://help.marklogic.com).
