import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    signing
}

group = "io.github.zjns"
archivesName.set("privacy-defender")
version = "0.2.1"

val mavenCentralUsername: String? by rootProject
val mavenCentralPassword: String? by rootProject

gradlePlugin {
    plugins {
        create("privacyDefenderPlugin") {
            id = project.group.toString() + "." + project.archivesName.get()
            implementationClass = "io.github.zjns.privacydefender.PrivacyDefender"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("privacyDefenderPlugin") {
            groupId = project.group.toString()
            artifactId = project.archivesName.get()
            version = project.version.toString()
            from(components.getByName("kotlin"))
            artifact(tasks.getByName("kotlinSourcesJar"))
            pom {
                packaging = "jar"
                name.set("Privacy Defender")
                description.set("A plugin to block sensitive Android framework api call.")
                url.set("https://github.com/zjns/PrivacyDefender")
                inceptionYear.set("2023")
                scm {
                    url.set("https://github.com/zjns/PrivacyDefender")
                    connection.set("scm:git:git://github.com/zjns/PrivacyDefender.git")
                    developerConnection.set("scm:git:ssh://git@github.com/zjns/PrivacyDefender.git")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("zjns")
                        name.set("Kofua")
                        email.set("1638183271zjn@gmail.com")
                    }
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/zjns/PrivacyDefender/issues")
                }
                ciManagement {
                    system.set("GitHub Actions")
                    url.set("https://github.com/zjns/PrivacyDefender/actions")
                }
            }
        }
    }

    repositories {
        maven {
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = mavenCentralUsername
                password = mavenCentralPassword
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.4")
    implementation("com.android.tools:common:30.4.1")
    implementation("org.ow2.asm:asm:9.3")
    implementation("org.ow2.asm:asm-commons:9.2")
}
