# rsync library for java

For more details check [Rsync](https://en.wikipedia.org/wiki/Rsync)

Use the following:
* java 17 (set through jenv, check `.java-version` file)
* maven wrapper with maven 3.6.3

## Build and run 

Use maven wrapper to build in package application 
```
./mvnw clean package
```

Execute `rsync` functionality for 2 local folders:
```
java -jar target/librsync-java-0.0.1-SNAPSHOT.jar sync-in sync-out
```

## Running unit tests.

```
./mvnw clean test
```

