import com.jfrog.bintray.gradle.BintrayExtension
import java.nio.file.Files
import java.nio.file.Paths

plugins {
  `java-library`
  `maven-publish`
  id("com.jfrog.bintray") version "1.8.5"
}

apply(from = "${projectDir.absolutePath}/dependencies.gradle.kts")
apply(from = "${projectDir.absolutePath}/test-summary.gradle")

java {
  targetCompatibility = JavaVersion.VERSION_14
  sourceCompatibility = JavaVersion.VERSION_14
}

// Project settings
group   = "org.veupathdb.lib"
version = "2.7.4"

repositories {
  jcenter()
  mavenCentral()
}

java {
  withSourcesJar()
  withJavadocJar()
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
