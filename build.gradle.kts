import com.jfrog.bintray.gradle.BintrayExtension
import java.nio.file.Files
import java.nio.file.Paths

plugins {
  java
  `maven-publish`
  id("com.jfrog.bintray") version "1.8.5"
}

// Project settings
group   = "org.veupathdb.lib"
version = "1.0.3"

// Versions
val versionLog4j   = "2.13.2"
val versionJackson = "2.11.0"
val versionJersey  = "2.30.1"
val versionJunit   = "5.6.2"

// Additional settings
val moduleName = "epvb.lib.container.jaxrs.core"
val patchArgs  = listOf(
  "--patch-module",
  "${moduleName}=${tasks.compileJava.get().destinationDirectory.asFile.get().path}"
)


repositories {
  jcenter()
}

java {
  withSourcesJar()
  withJavadocJar()
}

dependencies {

  //
  // FgpUtil & Compatibility Dependencies
  //

  // FgpUtil jars
  implementation(files(
    "vendor/fgputil-util-1.0.0.jar",
    "vendor/fgputil-accountdb-1.0.0.jar"
  ))

  // Compatibility bridge to support the long dead log4j-1.X
  runtimeOnly("org.apache.logging.log4j:log4j-1.2-api:${versionLog4j}")

  // Extra FgpUtil dependencies
  runtimeOnly("org.apache.commons:commons-dbcp2:2.7.0")

  //
  // Project Dependencies
  //

  // JavaX
  implementation("jakarta.ws.rs:jakarta.ws.rs-api:2.1.6")

  // Jersey
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:${versionJersey}")
  implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:${versionJersey}")
  implementation("org.glassfish.jersey.media:jersey-media-json-jackson:${versionJersey}")
  runtimeOnly("org.glassfish.jersey.inject:jersey-hk2:${versionJersey}")

  // Jackson
  implementation("com.fasterxml.jackson.core:jackson-databind:${versionJackson}")
  implementation("com.fasterxml.jackson.core:jackson-annotations:${versionJackson}")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${versionJackson}")

  // CLI
  implementation("info.picocli:picocli:4.2.0")
  annotationProcessor("info.picocli:picocli-codegen:4.2.0")

  // Log4J
  implementation("org.apache.logging.log4j:log4j-api:${versionLog4j}")
  implementation("org.apache.logging.log4j:log4j-core:${versionLog4j}")
  implementation("org.apache.logging.log4j:log4j:${versionLog4j}")

  // Metrics
  implementation("io.prometheus:simpleclient:0.9.0")
  implementation("io.prometheus:simpleclient_common:0.9.0")

  // Utils
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")
  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter-api:${versionJunit}")
  testImplementation("org.mockito:mockito-core:2.+")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${versionJunit}")
}

tasks.jar {
  manifest {
    attributes["Implementation-Title"]   = project.name
    attributes["Implementation-Version"] = project.version
  }
}

plugins.withType<JavaPlugin>().configureEach {
  configure<JavaPluginExtension> {
    modularity.inferModulePath.set(true)
  }
}

tasks.compileJava {
  options.compilerArgs.addAll(listOf(
    "--module-path", classpath.asPath
  ))
  classpath = files()
}
tasks.compileTestJava {
}

tasks.test {
  doFirst {
    Files.move(Paths.get(projectDir.absolutePath, "src/main/java/module-info.java"),
      Paths.get(projectDir.absolutePath, "src/main/java/module-info._"))
  }
  doLast {
    Files.move(Paths.get(projectDir.absolutePath, "src/main/java/module-info._"),
      Paths.get(projectDir.absolutePath, "src/main/java/module-info.java"))
  }
}

tasks.javadoc {
  doFirst {
    Files.move(Paths.get(projectDir.absolutePath, "src/main/java/module-info.java"),
      Paths.get(projectDir.absolutePath, "src/main/java/module-info._"))
  }
  doLast {
    Files.move(Paths.get(projectDir.absolutePath, "src/main/java/module-info._"),
      Paths.get(projectDir.absolutePath, "src/main/java/module-info.java"))
  }
}

val test by tasks.getting(Test::class) {
  // Use junit platform for unit tests
  useJUnitPlatform()
}

publishing {
  publications {
    create<MavenPublication>("gpr") {
      from(components["java"])
      pom {
        name.convention("JaxRS Container Core Library")
        description.set("Provides base methods, endpoints, server setup, and utilities for use in containerized VEuPathDB JaxRS based applications.")
        url.set("https://github.com/VEuPathDB/lib-jaxrs-container-core")
        developers {
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("epharper@upenn.edu")
            url.set("https://github.com/foxcapades/")
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

bintray {
  user = project.findProperty("bintray.user") as String? ?: ""
  key  = project.findProperty("bintray.pass") as String? ?: ""
  publish = true
  setPublications("gpr")
  pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
    repo = "maven"
    name = "lib-jaxrs-container-core"
    userOrg = "veupathdb"
    setVersion(rootProject.version)
  })
}
