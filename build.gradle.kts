import com.jfrog.bintray.gradle.BintrayExtension
import java.nio.file.Files
import java.nio.file.Paths

plugins {
  java
  `maven-publish`
  id("com.jfrog.bintray") version "1.8.5"
}

apply(from = "dependencies.gradle.kts")

// Project settings
group   = "org.veupathdb.lib"
version = "1.6.0"

// Additional settings
val moduleName = "epvb.lib.container.jaxrs.core"
val patchArgs  = listOf(
  "--patch-module",
  "${moduleName}=${tasks.compileJava.get().destinationDirectory.asFile.get().path}"
)

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

plugins.withType<JavaPlugin>().configureEach {
  configure<JavaPluginExtension> {
    modularity.inferModulePath.set(true)
  }
}

tasks.compileJava {
  doFirst {
    exec {
      commandLine("${projectDir.absolutePath}/bin/install-fgputil.sh",
        rootProject.projectDir.absolutePath)
    }
  }

  options.compilerArgs.addAll(listOf(
    "--module-path", classpath.asPath
  ))

  classpath = files()
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
