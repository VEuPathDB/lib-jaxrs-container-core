group   = "org.veupathdb.lib"
version = "10.0.2"

plugins {
  `java-library`
  `maven-publish`
}

apply(from = "${projectDir.absolutePath}/test-summary.gradle")

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21

  withSourcesJar()
  withJavadocJar()
}

repositories {
  maven {
    name = "GitHubPackages"
    url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
    credentials {
      username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
      password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
    content {
      includeGroupByRegex("org\\.(gusdb|veupathdb).*")
    }
  }

  mavenCentral()
}

tasks.register<Javadoc>("updateJavadocs") {
  source = sourceSets["main"].allJava
  classpath = sourceSets["main"].runtimeClasspath
  setDestinationDir(file("docs/javadoc"))
}

dependencies {
  api(libs.vpdb.oauth)
  api(libs.vpdb.prometheus)
  api(libs.log.slf4j)

  api(libs.bundles.jersey)
  api(platform(libs.jackson.bom))
  api(libs.bundles.jackson)

  implementation(libs.vpdb.fgputil)
  implementation(libs.vpdb.ldap)
  implementation(libs.vpdb.jackson.pojo)

  implementation(libs.bundles.log4j)
  implementation(libs.cli.code)
  implementation(libs.metrics.prometheus.client)
  implementation(libs.metrics.prometheus.common)
  implementation(libs.uid)

  runtimeOnly(libs.db.oracle)
  runtimeOnly(libs.db.postgres)

  annotationProcessor(libs.cli.processor)
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

testing {
  suites {
    withType<JvmTestSuite> {
      useJUnitJupiter(libs.versions.junit)
      dependencies {
        implementation(libs.mockito.core)
        implementation(libs.mockito.junit)
      }
    }
  }
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
