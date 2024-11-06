These are rough notes for now that will eventually be in the user guide instead of here. 

Get a zip file from https://www.loc.gov/item/2018487913/ . 

Build the shadow jar for this repo via `./gradlew shadowJar`.

Copy the shadow jar from `build/libs` to the `ext` folder of your Flux installation. 

Include the following options:

```
--splitter-custom-class "org.example.EnronEmailSplitter"
```
