FROM sbtscala/scala-sbt:openjdk-18.0.2.1_1.8.0_3.2.0

COPY src/main/scala/ /app/src/main/scala

COPY project/ /app/project

COPY build.sbt /app/build.sbt

WORKDIR /app/

RUN sbt update

RUN sbt compile

CMD sbt run
