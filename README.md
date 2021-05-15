# s3-notify


## Quick start

Download the [latest release]()  

Execute from the command line to trigger S3 notifications for a specific bucket/objects.  

Example -  

```
java -jar s3-notify.jar                                                     ─╯

 __ _____       __      _   _  __
/ _\___ /    /\ \ \___ | |_(_)/ _|_   _
\ \  |_ \   /  \/ / _ \| __| | |_| | | |
_\ \___) | / /\  / (_) | |_| |  _| |_| |
\__/____/  \_\ \/ \___/ \__|_|_|  \__, |
                                  |___/

https://www.linkedin.com/in/jeremybranham/
Savantly.net

           Powered by Quarkus 1.13.4.Final
2021-05-15 14:40:40,896 WARN  [io.qua.config] (main) Unrecognized configuration key "quarkus.package.uber-jar" was provided; it will be ignored; verify that the dependency extension for this configuration is set or that you did not make a typo
2021-05-15 14:40:42,868 INFO  [io.quarkus] (main) s3-notify 1.0.0-SNAPSHOT on JVM (powered by Quarkus 1.13.4.Final) started in 2.886s.
2021-05-15 14:40:42,870 INFO  [io.quarkus] (main) Profile prod activated.
2021-05-15 14:40:42,870 INFO  [io.quarkus] (main) Installed features: [amazon-s3, amazon-sns, cdi, picocli]

Usage: <main class> [-dv] -b=<bucket> [-e=<event>] [-m=<match>] [-p=<prefix>]
                    [-r=<region>] [-s=<destinations>]...
Simulates S3 event notifications
  -b, --bucket=<bucket>   The bucket name containing the S3 objects
  -d, --debug             Debug logging
  -e, --event=<event>     The event that should be simulated
  -m, --match=<match>     Only process objects with keys matching this regex
  -p, --prefix=<prefix>   Only process objects with this key prefix
  -r, --region=<region>   The origin region of the event that should be
                            simulated
  -s, --service=<destinations>
                          The services that should be sent a notification
  -v, --verbose           Verbose logging
2021-05-15 14:40:43,323 INFO  [io.quarkus] (main) s3-notify stopped in 0.095s
```

A native executable can be built, but I'm new to quarkus and haven't done this yet.  

Feel free to submit a PR!  



# Development

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/s3-touch-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

