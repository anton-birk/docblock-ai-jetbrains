package com.docblockai.actions

import com.docblockai.api.ClaudeClient
import com.docblockai.api.OpenAiClient
import com.docblockai.buildPrompt
import com.docblockai.resolveLanguage
import com.docblockai.settings.DocBlockSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange

class GenerateDocAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: run {
            Messages.showInfoMessage(project, "Cannot determine file language.", "DocBlock AI")
            return
        }

        val langId = resolveLanguage(
            psiFile.language.id,
            psiFile.virtualFile?.extension,
        ) ?: run {
            Messages.showInfoMessage(
                project,
                "DocBlock AI does not support \"${psiFile.language.id}\" files.",
                "DocBlock AI",
            )
            return
        }

        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText?.trim()
        if (selectedText.isNullOrBlank()) {
            Messages.showInfoMessage(
                project,
                "Please select a function, method, or class before generating documentation.",
                "DocBlock AI",
            )
            return
        }

        // Capture everything from the document on the EDT before going background.
        val document = editor.document
        val startLine = document.getLineNumber(selectionModel.selectionStart)
        val lineStartOffset = document.getLineStartOffset(startLine)

        val contextStartLine = maxOf(0, startLine - 100)
        val fileContext = document.getText(
            TextRange(document.getLineStartOffset(contextStartLine), lineStartOffset)
        ).trim()

        val lineText = document.getText(TextRange(lineStartOffset, document.getLineEndOffset(startLine)))
        val indent = lineText.takeWhile { it == ' ' || it == '\t' }

        val state = DocBlockSettings.getInstance().state.copy()
        val prompt = buildPrompt(selectedText, langId, state.language, fileContext)

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project, "DocBlock AI: generating documentation…", true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true

                val raw = try {
                    when (state.provider) {
                        "claude" -> ClaudeClient.generate(state.claudeApiKey, state.claudeModel, prompt)
                        else -> OpenAiClient.generate(state.openaiApiKey, state.openaiModel, prompt)
                    }
                } catch (ex: Exception) {
                    ui { Messages.showErrorDialog(project, ex.message ?: "Unknown error", "DocBlock AI") }
                    return
                }

                if (raw.isNullOrBlank()) {
                    ui { Messages.showInfoMessage(project, "Could not generate documentation for this code.", "DocBlock AI") }
                    return
                }

                val cleaned = cleanDoc(raw)
                val indented = cleaned.lines().joinToString("\n") { indent + it } + "\n"

                ui {
                    WriteCommandAction.runWriteCommandAction(project, "Generate DocBlock", null, {
                        if (!editor.isDisposed) {
                            document.insertString(lineStartOffset, indented)
                        }
                    })
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null
    }

    private fun cleanDoc(doc: String): String {
        val stripped = doc
            .replace(Regex("^```[\\w]*\\n?"), "")
            .replace(Regex("```$"), "")
            .replace(Regex("(?m)^/\\s+\\*\\*"), "/**")
            .replace(Regex("(?m)^/\\*{3,}"), "/**")
            .trim()

        // LLMs often emit extra leading spaces on * lines inside /**...*/ blocks,
        // e.g. "        * text" instead of " * text". Normalize each such line to
        // exactly one leading space so indentation stays correct after file-level
        // indent is applied. Other comment styles (///, #, """) are unaffected.
        return stripped.lines().joinToString("\n") { line ->
            val trimmed = line.trimStart()
            if (trimmed.startsWith("* ") || trimmed == "*" || trimmed.startsWith("*/")) {
                " $trimmed"
            } else {
                line
            }
        }
    }

    private fun ui(block: () -> Unit) =
        ApplicationManager.getApplication().invokeLater(block)
}