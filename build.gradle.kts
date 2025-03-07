group   = "org.veupathdb.lib"
version = "9.1.3"

plugins {
  `java-library`
  `maven-publish`
}

apply(from = "${projectDir.absolutePath}/test-summary.gradle")

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.AMAZON
  }

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
  implementation("org.gusdb:fgputil-db:2.16.0-jakarta")

  // OAuth Client
  api("org.gusdb:oauth2-client:3.2.1-jakarta")

  // Oracle
  runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")

  // Jersey
  api(platform("org.glassfish.jersey:jersey-bom:3.1.10"))
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http")
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet")
  runtimeOnly("org.glassfish.jersey.inject:jersey-hk2")
  implementation("org.glassfish.hk2:hk2-api:3.1.0")

  implementation("org.veupathdb.lib:multipart-jackson-pojo:1.1.7")

  // Jackson
  api(platform("com.fasterxml.jackson:jackson-bom:2.18.3"))
  api("com.fasterxml.jackson.core:jackson-core")
  api("com.fasterxml.jackson.core:jackson-databind")
  api("com.fasterxml.jackson.core:jackson-annotations")
  api("com.fasterxml.jackson.module:jackson-module-parameter-names")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
  api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

  // Logging
  api("org.slf4j:slf4j-api:2.0.16")
  implementation(platform("org.apache.logging.log4j:log4j-bom:2.24.3"))
  implementation("org.apache.logging.log4j:log4j-api")
  implementation("org.apache.logging.log4j:log4j-core")
  implementation("org.apache.logging.log4j:log4j-slf4j2-impl")

  // CLI
  implementation("info.picocli:picocli:4.7.6")
  annotationProcessor("info.picocli:picocli-codegen:4.7.6")

  // Metrics
  implementation("io.prometheus:simpleclient:0.16.0")
  implementation("io.prometheus:simpleclient_common:0.16.0")
  api("org.veupathdb.lib:lib-prometheus-stats:1.3.0")

  // Unique, human readable id generation
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")

  // LDAP utils
  implementation("com.unboundid:unboundid-ldapsdk:6.0.11")

  //
  // Testing Stuff
  //

  // Unit Testing
  testImplementation(platform("org.junit:junit-bom:5.12.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core:5.15.2")
  testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")
}

tasks.jar {
  manifest {
    attributes["Implementation-Title"]   = project.name
    attributes["Implementation-Version"] = project.version
  }
}

tasks.register("printVersion") {
  doLast {
    print(version)
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
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
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
