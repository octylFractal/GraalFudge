plugins {
    `java-library`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

dependencies {
    compileOnly(libs.checkerframework.qual)

    api(libs.graal.truffle.api)

    annotationProcessor(libs.graal.truffle.dslProcessor)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

tasks.test {
    useJUnitPlatform()
}
