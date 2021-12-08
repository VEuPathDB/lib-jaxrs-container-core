
plugins {
  `java-library`
  `maven-publish`
}

apply(from = "${projectDir.absolutePath}/dependencies.gradle.kts")
apply(from = "${projectDir.absolutePath}/test-summary.gradle")

java {
  targetCompatibility = JavaVersion.VERSION_15
  sourceCompatibility = JavaVersion.VERSION_15
}

// Project settings
group   = "org.veupathdb.lib"
version = "5.2.2"

repositories {
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

//tasks.register<JacocoReport>("codeCoverageReport") {
//  executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))
//
//  subprojects.onEach {
//    sourceSets(it.sourceSets["main"])
//  }
//
//  reports {
//    xml.isEnabled = true
//    xml.destination = File("${buildDir}/reports/jacoco/report.xml")
//    html.isEnabled = false
//    csv.isEnabled = false
//  }
//
//  dependsOn("test")
//}
