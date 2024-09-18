// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

repositories {
  mavenCentral()
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
  maven("https://www.jetbrains.com/intellij-repository/snapshots")
  maven("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
}

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

val pluginVersion = prop("plugin.version")
val ideType = prop("ide.type")
val ideVersion = prop("ide.version")
val ideSinceVersion = prop("ide.since")
val ideUntilVersion = prop("ide.until")

group = "org.analogjs."
version = pluginVersion

intellij {
  pluginName.set("Analog")
  plugins.set(listOf("JavaScript", "JSIntentionPowerPack", "HtmlTools", "com.intellij.css", "uml", "tslint", "intellij.webpack", "AngularJS"))

  version.set(ideVersion)
  type.set(ideType)
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src", "gen"))
    }
    resources {
      setSrcDirs(listOf("resources"))
    }
  }
  test {
    java {
      //setSrcDirs(listOf("test"))
    }
  }
}

dependencies {
  //testImplementation("com.jetbrains.intellij.javascript:javascript-test-framework:LATEST-EAP-SNAPSHOT")
  //testImplementation("com.jetbrains.intellij.copyright:copyright:LATEST-EAP-SNAPSHOT")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
  }
  wrapper {
    gradleVersion = "8.5"
  }
  runIde {
    autoReloadPlugins.set(false)
  }
  patchPluginXml {
    sinceBuild.set(ideSinceVersion)
  }
}

fun prop(name: String): String =
  extra.properties[name] as? String
  ?: error("Property `$name` is not defined in gradle.properties")