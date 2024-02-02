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

intellij {
  pluginName.set("Analog")
  plugins.set(listOf("JavaScript", "JSIntentionPowerPack", "HtmlTools", "com.intellij.css", "uml", "tslint", "intellij.webpack", "AngularJS"))

  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IU")
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
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")