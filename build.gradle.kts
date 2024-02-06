
// Project settings
group   = "org.veupathdb.lib"
version = "7.0.0-alpha1"

plugins {
  `java-library`
  `maven-publish`
}

apply(from = "${projectDir.absolutePath}/test-summary.gradle")

java {
  targetCompatibility = JavaVersion.VERSION_15
  sourceCompatibility = JavaVersion.VERSION_15
  withSourcesJar()
  withJavadocJar()
}

repositories {
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
    credentials {
      username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
      password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
  }
}

dependencies {

  // // // // // // // // // // // // // // // // // // // // // // // // // //
  //
  // Project Dependencies
  //
  // // // // // // // // // // // // // // // // // // // // // // // // // //


  // FgpUtil
  val fgputil = "2.12.11-jakarta"
  implementation("org.gusdb:fgputil-core:${fgputil}")
  implementation("org.gusdb:fgputil-db:${fgputil}")
  implementation("org.gusdb:fgputil-web:${fgputil}")
  implementation("org.gusdb:fgputil-accountdb:${fgputil}")

  // OAuth Client
  implementation("org.gusdb:oauth2-client:0.7.1")

  // Oracle
  runtimeOnly("com.oracle.database.jdbc:ojdbc8:21.9.0.0")

  // Jersey
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:3.1.1")
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:3.1.1")
  implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.1")
  runtimeOnly("org.glassfish.jersey.inject:jersey-hk2:3.1.1")
  implementation("org.glassfish.hk2:hk2-api:3.0.3")

  implementation("org.veupathdb.lib:multipart-jackson-pojo:1.1.1")

  // Jackson
  api("com.fasterxml.jackson.core:jackson-core:2.15.3")
  api("com.fasterxml.jackson.core:jackson-databind:2.15.3")
  api("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
  api("com.fasterxml.jackson.module:jackson-module-parameter-names:2.15.3")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.3")
  api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.3")

  // Log4J
  implementation("org.apache.logging.log4j:log4j-api:2.20.0")
  implementation("org.apache.logging.log4j:log4j-core:2.20.0")

  // CLI
  implementation("info.picocli:picocli:4.7.3")
  annotationProcessor("info.picocli:picocli-codegen:4.7.3")

  // Metrics
  implementation("io.prometheus:simpleclient:0.16.0")
  implementation("io.prometheus:simpleclient_common:0.16.0")
  api("org.veupathdb.lib:lib-prometheus-stats:1.2.3")

  // Unique, human readable id generation
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")

  // LDAP utils
  implementation("com.unboundid:unboundid-ldapsdk:6.0.8")

  // Query stuff
  implementation("io.vulpine.lib:lib-query-util:2.1.0")
  implementation("io.vulpine.lib:sql-import:0.2.1")

  //
  // Testing Stuff
  //

  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
  testImplementation("org.mockito:mockito-core:5.2.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.2.0")
}

tasks.jar {
  manifest {
    attributes["Implementation-Title"]   = project.name
    attributes["Implementation-Version"] = project.version
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
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("epharper@upenn.edu")
            url.set("https://github.com/foxcapades")
            organization.set("VEuPathDB")
          }
          developer {
            id.set("rdoherty")
            name.set("Ryan Doherty")
            email.set("rdoherty@upenn.edu")
            url.set("https://github.com/ryanrdoherty")
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
