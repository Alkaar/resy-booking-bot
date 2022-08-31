name := "resy-booking-bot"

scalaVersion := "2.13.8"

lazy val root = project in file(".")

semanticdbEnabled := true

scalacOptions += "-Ywarn-unused"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.vovapolu" %% "scaluzzi"         % "0.1.23",
  "org.scalatest"       %% "autofix"          % "3.1.0.1",
  "com.eed3si9n.fix"    %% "scalafix-noinfer" % "0.1.0-M1"
)

libraryDependencies ++= Seq(
  "com.typesafe.play"        %% "play-ahc-ws"     % "2.8.16",
  "org.apache.logging.log4j" %% "log4j-api-scala" % "12.0",
  "org.apache.logging.log4j"  % "log4j-core"      % "2.13.0" % Runtime,
  "org.scalatest"            %% "scalatest"       % "3.2.12" % Test,
  "org.mockito"               % "mockito-core"    % "4.6.1"  % Test,
  "org.slf4j"                 % "slf4j-nop"       % "1.7.36" // Removes failed to load class warning
)
