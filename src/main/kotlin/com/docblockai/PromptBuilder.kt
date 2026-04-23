package com.docblockai

fun buildPrompt(code: String, langId: String, docLanguage: String, fileContext: String): String {
    val format = DOC_FORMATS[langId] ?: DocFormat("documentation comment")
    val contextBlock = if (fileContext.isNotBlank())
        "\nFile context (code before the element — use it to understand the class, dependencies, and purpose):\n```\n$fileContext\n```\n\n"
    else "\n"

    return """You are a senior software engineer writing ${format.name} documentation.

Task: write a ${format.name} comment for the code element below. The element may be a function, method, class, interface, variable, constant, or property.

Rules:
- Return ONLY the documentation comment block, nothing else — no code, no explanation, no markdown fences
- The comment must start exactly with the correct opening (/** for PHPDoc/JSDoc/Javadoc/KDoc, ${"\"\"\""}  for Python, // for Go, etc.)
- Write ALL descriptive text in $docLanguage language
- Description must be 2–3 short sentences: first sentence says what the element does, the next 1–2 sentences briefly explain the key details (inputs processed, main steps, notable behaviour). No more than that.
- Wrap description lines at 80 characters — continue on the next comment line with the same prefix (e.g. " * " for PHPDoc)
- For functions and methods: include all @param tags with exact types and meaningful descriptions; for @return/@returns list every possible return value with a clear explanation of when each occurs; include @throws if exceptions are possible
- Separate each tag group with a blank comment line (e.g. " * " on its own line for PHPDoc/JSDoc): after all @param tags add a blank line before @return, then a blank line before @throws
- For classes and interfaces: describe the purpose; if abstract, mention what subclasses must implement
- For variables, constants, and properties: describe what the value represents and how it is used; include the type if the format supports it
- Use the file context to understand the class hierarchy, properties, and dependencies
${contextBlock}Element to document:
```
$code
```"""
}
