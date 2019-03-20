name := "akka"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.21"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster"         % akkaVersion,
  "com.typesafe.akka" %% "akka-remote"          % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"           % akkaVersion,
  "com.typesafe.akka" %% "akka-http"            % "10.1.7",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion % Test,
  "org.scalatest"     %% "scalatest"            % "3.0.6" % Test
)