plugins {
    java
    id("com.gradleup.shadow") version "8.3.9"
}

group = "aedifi.chatprofiler"
version = providers.gradleProperty("version").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val templatesDir = layout.projectDirectory.dir("src/main/templates")
val generatedTemplatesDir = layout.buildDirectory.dir("generated/sources/java-templates")

sourceSets {
    main {
        java.srcDir(generatedTemplatesDir)
    }
}

val processTemplates = tasks.register<Copy>("processTemplates") {
    from(templatesDir)
    into(generatedTemplatesDir)
    val v = project.version.toString()
    filter { line: String -> line.replace("\${version}", v) }
}

tasks.compileJava {
    dependsOn(processTemplates)
}

tasks.clean {
    delete(generatedTemplatesDir)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:${providers.gradleProperty("velocityApiVersion").get()}")
    annotationProcessor("com.velocitypowered:velocity-api:${providers.gradleProperty("velocityApiVersion").get()}")

    implementation("com.github.retrooper:packetevents-api:${providers.gradleProperty("packetEventsVersion").get()}")
    implementation("com.github.retrooper:packetevents-velocity:${providers.gradleProperty("packetEventsVersion").get()}")
    implementation("dev.dejvokep:boosted-yaml:1.3.7")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("plain")
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
