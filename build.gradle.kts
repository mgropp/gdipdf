plugins {
    java
}

group = "de.fau.cs.gdi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java.sourceSets["main"].java {
    srcDir("src")
    srcDir("src-generated")
}

dependencies {
    implementation("com.ibm.icu:icu4j:4.8.1.1")
    implementation("com.itextpdf:itextpdf:5.5.9")
    implementation("args4j:args4j:2.0.16")
    implementation("org.apache.commons:commons-lang3:3.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}