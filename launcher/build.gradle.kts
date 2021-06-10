plugins {
    java
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

dependencies {
    compileOnly(libs.checkerframework.qual)

    implementation(libs.graal.sdk)
    implementation(libs.picocli.core)
    annotationProcessor(libs.picocli.codegen)
}

tasks.compileJava.configure {
    options.compilerArgs.add("-Aproject=${project.group}/GraalFudge")
}

val mainClassValue = "net.octyl.graalfudge.launcher.GraalFudgeLauncher"
tasks.jar.configure {
    manifest.attributes["Main-Class"] = mainClassValue
}

tasks.register<JavaExec>("run") {
    val jarFile = project(":language").tasks.named("jar")
    dependsOn(jarFile)
    workingDir(rootProject.projectDir)
//    executable("/usr/lib/jvm/java-16-graalvm/bin/java")
    mainClass.set(mainClassValue)
    modularity.inferModulePath.set(true)
    classpath(
        configurations["runtimeClasspath"],
        project(":language").configurations["runtimeClasspath"],
        sourceSets["main"].output,
        jarFile
    )
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+EnableJVMCI",
            "-XX:+UseJVMCICompiler",
            "-Dtruffle.class.path.append=${jarFile.get().outputs.files.singleFile}"
        )
    })
}
