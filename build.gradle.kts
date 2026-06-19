plugins {
    java
}

group = "com.panita.tezzlar3"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    // Paper API for version 26.2 (Experimental)
    compileOnly("io.papermc.paper:paper-api:26.2.build.24-alpha")
    
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")
    
    // Adventure API for component-based text
    compileOnly("net.kyori:adventure-api:4.20.0")
    
    // Reflections for dynamic command/listener registration
    implementation("org.reflections:reflections:0.10.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

// Bundle all runtime dependencies into the final JAR natively to bypass Shadow/ASM limitations with Java 25
tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
