Including the same 5 files as the latest MLCP. Testing has indicated that we may only need
msvcr100.dll and hadoop.dll. But including the same 5 files as MLCP in the event that our tests
haven't covered the need for those 5 files.

As of Spark 4.1.0-preview2 - the hadoop.dll and msvcr100.dll files are still needed when running Flux on Windows. The
other files are still being retained just in case.
