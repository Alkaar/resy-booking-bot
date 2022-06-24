name := "resy-booking-bot"

scalaVersion := "2.12.8"

lazy val root = project in file(".")

libraryDependencies ++= Seq("com.typesafe.play" %% "play-ahc-ws" % "2.8.9")

scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
)