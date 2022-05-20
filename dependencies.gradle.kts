val jersey    = "2.33"   // Jersey/JaxRS version
val jackson   = "2.13.0" // FasterXML Jackson version
val junit     = "5.7.1"  // JUnit version
val log4j     = "2.17.2" // Log4J version
val fgputil   = "2.5"    // FgpUtil version

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

  implementation("org.gusdb:fgputil-core:${fgputil}")
  implementation("org.gusdb:fgputil-db:${fgputil}")
  implementation("org.gusdb:fgputil-accountdb:${fgputil}")
  implementation("org.gusdb:fgputil-web:${fgputil}")

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
  implementation("org.glassfish.hk2:hk2-api:2.6.1")
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
  implementation("info.picocli:picocli:4.5.1")
  annotationProcessor("info.picocli:picocli-codegen:4.5.1")

  // Metrics
  implementation("io.prometheus:simpleclient:0.9.0")
  implementation("io.prometheus:simpleclient_common:0.9.0")

  //
  // Utils
  //

  // Unique, human readable id genderation
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")

  // LDAP utils
  implementation("com.unboundid:unboundid-ldapsdk:5.1.0")

  // Query stuff
  implementation("io.vulpine.lib:lib-query-util:2.1.0")
  implementation("io.vulpine.lib:sql-import:0.2.1")

  //
  // Testing Stuff
  //

  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter:${junit}")
  testImplementation("org.mockito:mockito-core:2.+")
}
