name := "resy-booking-bot"

scalaVersion := "2.13.8"

lazy val root = project in file(".")

semanticdbEnabled := true

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws"  % "2.8.16",
  "org.scalatest"     %% "scalatest"    % "3.2.10" % Test,
  "org.mockito"        % "mockito-core" % "4.6.1"  % Test
)
