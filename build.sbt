name := "akkaTipsAndTricks"

version := "v0.1"

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.2"
libraryDependencies += "com.typesafe.akka" % "akka-stream_2.11" % "2.4.2"
libraryDependencies += "com.typesafe.akka" % "akka-http-core_2.11" % "2.4.2"
libraryDependencies += "com.typesafe.akka" % "akka-agent_2.11" % "2.4.2"



//logLevel := Level.Debug

scalacOptions += "-deprecation"

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "." + artifact.extension
}
