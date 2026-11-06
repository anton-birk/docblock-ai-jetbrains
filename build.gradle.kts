plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
}

group = "com.docblockai"
version = "1.0.1"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    implementation("com.google.code.gson:gson:2.10.1")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
        changeNotes = """
            <ul>
                <li>API keys are now stored in the IDE password safe instead of plain-text settings (existing keys are migrated automatically).</li>
                <li>Added Claude Sonnet 5.5, Opus 5.5 and Fable 5.1; Claude Sonnet 5.5 is the new default.</li>
                <li>Added GPT-6.1 Terra, GPT-6.1 Sol, GPT-5.5 and GPT-5.4 mini; GPT-6.1 Terra is the new OpenAI default.</li>
                <li>Removed retired OpenAI models (gpt-4-turbo, gpt-3.5-turbo, gpt-4o); settings using them switch to GPT-6.1 Terra.</li>
                <li>Fixed empty results with Claude models that think before answering; Claude refusals now show a clear message.</li>
                <li>Added connection and request timeouts so generation no longer hangs on network issues.</li>
                <li>Action availability is now computed on a background thread.</li>
            </ul>
        """.trimIndent()
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
}