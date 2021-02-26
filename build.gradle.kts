import com.jfrog.bintray.gradle.BintrayExtension

plugins {
  `java-library`
  `maven-publish`
//  jacoco// version "0.8.7-SNAPSHOT"
  id("com.jfrog.bintray") version "1.8.5"
}

apply(from = "${projectDir.absolutePath}/dependencies.gradle.kts")
apply(from = "${projectDir.absolutePath}/test-summary.gradle")

java {
  targetCompatibility = JavaVersion.VERSION_15
  sourceCompatibility = JavaVersion.VERSION_15
}

// Project settings
group   = "org.veupathdb.lib"
version = "4.4.0"

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
