This project shows how to implement and build an implementation of the langchain4j
[DocumentSplitter](https://docs.langchain4j.dev/apidocs/dev/langchain4j/data/document/DocumentSplitter.html) interface. 
This implementation can then be used with the `--splitter-custom-class` option in Flux commands that support
splitting the text in documents. 

This example implementation is intended for use with the [Enron email dataset](https://www.loc.gov/item/2018487913/). 
It has some simple logic in it for attempting to ignore the headers in each email. It is intended to be primarily used
as an example and is not intended to be a completely accurate email parser. 

If you would like to download the Enron email dataset from the above link and test it with this custom splitter, 
perform the following steps, using Java 11 or higher: 

1. Clone this repository using git if you have not already.
2. From the directory containing this README file, run `../gradlew shadowJar` to produce a single jar file containing
the custom splitter implementation. 
3. Copy the shadow jar file in `./build/libs` to the `ext` folder of your Flux installation. 
4. When importing the Enron email dataset, include `--splitter-custom-class "org.example.EnronEmailSplitter"` as 
a command line option.
