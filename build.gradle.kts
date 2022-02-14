
plugins {
  `java-library`
  `maven-publish`
}

apply(from = "${projectDir.absolutePath}/test-summary.gradle")

java {
  targetCompatibility = JavaVersion.VERSION_15
  sourceCompatibility = JavaVersion.VERSION_15
}

// Project settings
group   = "org.veupathdb.lib"
version = "6.2.0"

repositories {
  mavenCentral()
}

java {
  withSourcesJar()
  withJavadocJar()
}

dependencies {

  // // // // // // // // // // // // // // // // // // // // // // // // // //
  //
  // FgpUtil & Compatibility Dependencies
  //
  // // // // // // // // // // // // // // // // // // // // // // // // // //

  // FgpUtil jars
  implementation(files(
    "${rootProject.projectDir.absolutePath}/vendor/fgputil-accountdb-1.0.0.jar",
    "${rootProject.projectDir.absolutePath}/vendor/fgputil-core-1.0.0.jar",
    "${rootProject.projectDir.absolutePath}/vendor/fgputil-db-1.0.0.jar",
    "${rootProject.projectDir.absolutePath}/vendor/fgputil-web-1.0.0.jar"
  ))

  // Compatibility bridge to support the long dead log4j-1.X
  runtimeOnly("org.apache.logging.log4j:log4j-1.2-api:2.17.0")

  // Extra FgpUtil dependencies
  runtimeOnly("org.apache.commons:commons-dbcp2:2.8.0")

  // // // // // // // // // // // // // // // // // // // // // // // // // //
  //
  // Project Dependencies
  //
  // // // // // // // // // // // // // // // // // // // // // // // // // //

  //
  // Server Stuff
  //

  // JaxRS
  implementation("jakarta.platform:jakarta.jakartaee-web-api:9.1.0")

  // Jersey
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:3.0.3")
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:3.0.3")
  implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.0.3")
  implementation("org.glassfish.hk2:hk2-api:3.0.2")
  runtimeOnly("org.glassfish.jersey.inject:jersey-hk2:3.0.3")

  //
  // (De)Serialization stuff
  //

  // Jackson
  implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
  implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.1")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.1")

  //
  // Logging Stuff
  //

  // Log4J
  implementation("org.apache.logging.log4j:log4j-api:2.17.0")
  implementation("org.apache.logging.log4j:log4j-core:2.17.0")
  implementation("org.apache.logging.log4j:log4j:2.16.0")

  //
  // Miscellaneous Stuff
  //

  // CLI
  implementation("info.picocli:picocli:4.6.2")
  annotationProcessor("info.picocli:picocli-codegen:4.6.2")

  // Metrics
  implementation("io.prometheus:simpleclient:0.14.1")
  implementation("io.prometheus:simpleclient_common:0.14.1")

  //
  // Utils
  //

  // Unique, human readable id genderation
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")

  // LDAP utils
  implementation("com.unboundid:unboundid-ldapsdk:6.0.3")

  // Query stuff
  implementation("io.vulpine.lib:lib-query-util:2.1.0")
  implementation("io.vulpine.lib:sql-import:0.2.1")

  //
  // Testing Stuff
  //

  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
  testImplementation("org.mockito:mockito-core:4.3.1")
}

tasks.jar {
  manifest {
    attributes["Implementation-Title"]   = project.name
    attributes["Implementation-Version"] = project.version
  }
}

tasks.compileJava {
  doFirst {
    exec {
      commandLine("${projectDir.absolutePath}/bin/install-fgputil.sh",
        rootProject.projectDir.absolutePath)
    }
  }
}

val test by tasks.getting(Test::class) {
  // Use junit platform for unit tests
  useJUnitPlatform()
}

publishing {
  repositories {
    maven {
      name = "GitHub"
      url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
    }
  }

  publications {
    create<MavenPublication>("gpr") {
      from(components["java"])
      pom {
        name.set("JaxRS Container Core Library")
        description.set("Provides base methods, endpoints, server setup, and utilities for use in containerized VEuPathDB JaxRS based applications.")
        url.set("https://github.com/VEuPathDB/lib-jaxrs-container-core")
        developers {
          developer {
            id.set("rdoherty")
            name.set("Ryan Doherty")
            email.set("rdoherty@upenn.edu")
            url.set("https://github.com/ryanrdoherty")
            organization.set("VEuPathDB")
          }
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("epharper@upenn.edu")
            url.set("https://github.com/foxcapades")
            organization.set("VEuPathDB")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/VEuPathDB/lib-jaxrs-container-core.git")
          developerConnection.set("scm:git:ssh://github.com/VEuPathDB/lib-jaxrs-container-core.git")
          url.set("https://github.com/VEuPathDB/lib-jaxrs-container-core")
        }
      }
    }
  }
}
