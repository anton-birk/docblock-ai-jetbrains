package com.docblockai.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.*

class DocBlockSettingsConfigurable : Configurable {

    private var panel: JPanel? = null

    // API keys as last loaded from PasswordSafe, so isModified() doesn't hit the credential store.
    private var savedOpenaiKey = ""
    private var savedClaudeKey = ""

    private val providerCombo = JComboBox(arrayOf("openai", "claude"))
    private val openaiKeyField = JPasswordField(40)
    private val openaiModelCombo = JComboBox(DocBlockSettings.OPENAI_MODELS.toTypedArray())
    private val claudeKeyField = JPasswordField(40)
    private val claudeModelCombo = JComboBox(arrayOf(
        "claude-sonnet-5-5", "claude-opus-5-5", "claude-fable-5-1",
        "claude-opus-4-7", "claude-sonnet-4-6",
    ))
    private val languageCombo = JComboBox(arrayOf(
        "English", "Russian", "Spanish", "Italian", "French",
        "German", "Portuguese", "Polish", "Chinese", "Japanese",
    ))

    override fun getDisplayName() = "DocBlock AI"

    override fun createComponent(): JComponent {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("AI provider:"), providerCombo, 1, false)
            .addSeparator(8)
            .addLabeledComponent(JBLabel("OpenAI API key:"), openaiKeyField, 1, false)
            .addLabeledComponent(JBLabel("OpenAI model:"), openaiModelCombo, 1, false)
            .addSeparator(8)
            .addLabeledComponent(JBLabel("Anthropic API key:"), claudeKeyField, 1, false)
            .addLabeledComponent(JBLabel("Claude model:"), claudeModelCombo, 1, false)
            .addSeparator(8)
            .addLabeledComponent(JBLabel("Documentation language:"), languageCombo, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        return panel!!
    }

    override fun isModified(): Boolean {
        val s = DocBlockSettings.getInstance().state
        return providerCombo.selectedItem != s.provider
            || String(openaiKeyField.password) != savedOpenaiKey
            || openaiModelCombo.selectedItem != s.openaiModel
            || String(claudeKeyField.password) != savedClaudeKey
            || claudeModelCombo.selectedItem != s.claudeModel
            || languageCombo.selectedItem != s.language
    }

    override fun apply() {
        val settings = DocBlockSettings.getInstance()
        val s = settings.state
        s.provider = providerCombo.selectedItem as String
        savedOpenaiKey = String(openaiKeyField.password)
        settings.openaiApiKey = savedOpenaiKey
        s.openaiModel = openaiModelCombo.selectedItem as String
        savedClaudeKey = String(claudeKeyField.password)
        settings.claudeApiKey = savedClaudeKey
        s.claudeModel = claudeModelCombo.selectedItem as String
        s.language = languageCombo.selectedItem as String
    }

    override fun reset() {
        val settings = DocBlockSettings.getInstance()
        val s = settings.state
        savedOpenaiKey = settings.openaiApiKey
        savedClaudeKey = settings.claudeApiKey
        providerCombo.selectedItem = s.provider
        openaiKeyField.text = savedOpenaiKey
        openaiModelCombo.selectedItem = s.openaiModel
        claudeKeyField.text = savedClaudeKey
        claudeModelCombo.selectedItem = s.claudeModel
        languageCombo.selectedItem = s.language
    }
}