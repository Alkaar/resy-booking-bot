name := "resy-booking-bot"

lazy val root = (project in file("."))

libraryDependencies ++= Seq("com.typesafe.play" %% "play-ahc-ws" % "2.6.11")