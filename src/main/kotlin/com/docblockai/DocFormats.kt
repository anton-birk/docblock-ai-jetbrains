package com.docblockai

data class DocFormat(val name: String)

val DOC_FORMATS: Map<String, DocFormat> = mapOf(
    "PHP"        to DocFormat("PHPDoc"),
    "JavaScript" to DocFormat("JSDoc"),
    "TypeScript" to DocFormat("TSDoc"),
    "Python"     to DocFormat("Google-style docstring"),
    "JAVA"       to DocFormat("Javadoc"),
    "Kotlin"     to DocFormat("KDoc"),
    "go"         to DocFormat("GoDoc comment"),
    "ruby"       to DocFormat("YARD documentation"),
    "Rust"       to DocFormat("Rust doc comment"),
    "C#"         to DocFormat("XML documentation comment"),
    "ObjectiveC" to DocFormat("HeaderDoc"),
    "Swift"      to DocFormat("Swift documentation comment"),
)

private val EXT_TO_LANG = mapOf(
    "php"  to "PHP",
    "js"   to "JavaScript",
    "jsx"  to "JavaScript",
    "ts"   to "TypeScript",
    "tsx"  to "TypeScript",
    "py"   to "Python",
    "java" to "JAVA",
    "kt"   to "Kotlin",
    "kts"  to "Kotlin",
    "go"   to "go",
    "rb"   to "ruby",
    "rs"   to "Rust",
    "cs"   to "C#",
    "m"    to "ObjectiveC",
    "swift" to "Swift",
)

fun resolveLanguage(langId: String, ext: String?): String? =
    DOC_FORMATS.keys.find { it.equals(langId, ignoreCase = true) }
        ?: ext?.lowercase()?.let { EXT_TO_LANG[it] }