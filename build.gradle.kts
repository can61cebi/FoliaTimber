plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "com.can61cebi"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.tcoded.com/releases")
    maven("https://jitpack.io")
    maven("https://maven.playpro.com")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("com.tcoded:FoliaLib:0.5.1")
    compileOnly("net.coreprotect:coreprotect:22.4")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.shadowJar {
    archiveBaseName.set("FoliaTimber")
    archiveClassifier.set("")
    relocate("com.tcoded.folialib", "com.can61cebi.foliatimber.lib.folialib")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

bukkit {
    name = "FoliaTimber"
    main = "com.can61cebi.foliatimber.FoliaTimber"
    apiVersion = "1.21"
    foliaSupported = true
    softDepend = listOf("CoreProtect", "WorldGuard")
    authors = listOf("can61cebi")
    description = "Smart tree chopping with structure protection for Folia"
    
    commands {
        register("timber") {
            description = "Toggle timber feature"
            usage = "/timber [toggle|reload]"
            permission = "foliatimber.use"
        }
    }
    
    permissions {
        register("foliatimber.use") {
            description = "Use FoliaTimber"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("foliatimber.bypass") {
            description = "Bypass structure protection"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.FALSE
        }
        register("foliatimber.reload") {
            description = "Reload configuration"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
    }
}
