name := "resy-booking-bot"

scalaVersion := "3.1.3"

lazy val root = project in file(".")

semanticdbEnabled := true

libraryDependencies ++= Seq(
  ("com.typesafe.play" %% "play-ahc-ws" % "2.8.16").cross(CrossVersion.for3Use2_13)
)
