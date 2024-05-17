addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.7.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.16.0")

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests" % "0.8.2",
  "com.lihaoyi" %% "os-lib" % "0.10.0",
)
