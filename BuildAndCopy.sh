#!/bin/bash
mvn clean install || exit 2000
mvn -o -f BEAST-Server/target/clients/java/pom.xml install || exit 2000

ssh controller@pc-sse-handycontroller "rm Server/*.jar Server/lib/*.jar" || exit 2000
scp -r BEAST-Server/target/lib controller@pc-sse-handycontroller:/home/controller/Server || exit 2000
scp -r BEAST-Server/target/BEAST-Server-*-SNAPSHOT.jar controller@pc-sse-handycontroller:/home/controller/Server/Server.jar || exit 2000