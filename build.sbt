name := "resy-booking-bot"

scalaVersion := "2.13.8"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.vovapolu" %% "scaluzzi"         % "0.1.23",
  "org.scalatest"       %% "autofix"          % "3.1.0.1",
  "com.eed3si9n.fix"    %% "scalafix-noinfer" % "0.1.0-M1"
)

val root = Project("resy-booking-bot", file("."))
  .settings(
    semanticdbEnabled := true,
    scalacOptions += "-Ywarn-unused",
    libraryDependencies ++= Seq(
      "com.typesafe.play"        %% "play-ahc-ws"     % "2.8.18",
      "org.apache.logging.log4j" %% "log4j-api-scala" % "12.0",
      "org.apache.logging.log4j"  % "log4j-core"      % "2.19.0" % Runtime,
      "org.scalatest"            %% "scalatest"       % "3.2.15" % Test,
      "org.mockito"               % "mockito-core"    % "5.1.1"  % Test,
      "org.slf4j"                 % "slf4j-nop"       % "2.0.5"
      // The above removes failed to load class warning
    ),
    publish := {},
    publishLocal := {}
  )
