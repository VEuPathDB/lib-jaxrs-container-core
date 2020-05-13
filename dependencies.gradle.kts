val jackson = "2.11.0"
val jersey  = "2.30.1"
val junit   = "5.6.2"
val log4j   = "2.13.2"

val implementation      by configurations
val testImplementation  by configurations
val runtimeOnly         by configurations
val annotationProcessor by configurations
val testRuntimeOnly     by configurations

dependencies {

  // // // // // // // // // // // // // // // // // // // // // // // // // //
  //
  // FgpUtil & Compatibility Dependencies
  //
  // // // // // // // // // // // // // // // // // // // // // // // // // //

  // FgpUtil jars
  implementation(files(
    "${rootProject.projectDir.absolutePath}/vendor/fgputil-util-1.0.0.jar",
    "${rootProject.projectDir.absolutePath}/vendor/fgputil-accountdb-1.0.0.jar"
  ))

  // Compatibility bridge to support the long dead log4j-1.X
  runtimeOnly("org.apache.logging.log4j:log4j-1.2-api:${log4j}")

  // Extra FgpUtil dependencies
  runtimeOnly("org.apache.commons:commons-dbcp2:2.7.0")

  // // // // // // // // // // // // // // // // // // // // // // // // // //
  //
  // Project Dependencies
  //
  // // // // // // // // // // // // // // // // // // // // // // // // // //

  //
  // Server Stuff
  //

  // JaxRS
  implementation("jakarta.ws.rs:jakarta.ws.rs-api:2.1.6")

  // Jersey
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:${jersey}")
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:${jersey}")
  implementation("org.glassfish.jersey.media:jersey-media-json-jackson:${jersey}")
  runtimeOnly("org.glassfish.jersey.inject:jersey-hk2:${jersey}")

  //
  // (De)Serialization stuff
  //

  // Jackson
  implementation("com.fasterxml.jackson.core:jackson-databind:${jackson}")
  implementation("com.fasterxml.jackson.core:jackson-annotations:${jackson}")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jackson}")

  //
  // Logging Stuff
  //

  // Log4J
  implementation("org.apache.logging.log4j:log4j-api:${log4j}")
  implementation("org.apache.logging.log4j:log4j-core:${log4j}")
  implementation("org.apache.logging.log4j:log4j:${log4j}")

  //
  // Miscellaneous Stuff
  //

  // CLI
  implementation("info.picocli:picocli:4.2.0")
  annotationProcessor("info.picocli:picocli-codegen:4.2.0")

  // Metrics
  implementation("io.prometheus:simpleclient:0.9.0")
  implementation("io.prometheus:simpleclient_common:0.9.0")

  // Utils
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")

  //
  // Testing Stuff
  //

  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter-api:${junit}")
  testImplementation("org.mockito:mockito-core:2.+")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junit}")
}
