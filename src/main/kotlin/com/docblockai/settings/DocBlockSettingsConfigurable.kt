package com.docblockai.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.*

class DocBlockSettingsConfigurable : Configurable {

    private var panel: JPanel? = null

    private val providerCombo = JComboBox(arrayOf("openai", "claude"))
    private val openaiKeyField = JPasswordField(40)
    private val openaiModelCombo = JComboBox(arrayOf("gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo"))
    private val claudeKeyField = JPasswordField(40)
    private val claudeModelCombo = JComboBox(arrayOf("claude-opus-4-7", "claude-sonnet-4-6", "claude-haiku-4-5-20251001"))
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
            || String(openaiKeyField.password) != s.openaiApiKey
            || openaiModelCombo.selectedItem != s.openaiModel
            || String(claudeKeyField.password) != s.claudeApiKey
            || claudeModelCombo.selectedItem != s.claudeModel
            || languageCombo.selectedItem != s.language
    }

    override fun apply() {
        val s = DocBlockSettings.getInstance().state
        s.provider = providerCombo.selectedItem as String
        s.openaiApiKey = String(openaiKeyField.password)
        s.openaiModel = openaiModelCombo.selectedItem as String
        s.claudeApiKey = String(claudeKeyField.password)
        s.claudeModel = claudeModelCombo.selectedItem as String
        s.language = languageCombo.selectedItem as String
    }

    override fun reset() {
        val s = DocBlockSettings.getInstance().state
        providerCombo.selectedItem = s.provider
        openaiKeyField.text = s.openaiApiKey
        openaiModelCombo.selectedItem = s.openaiModel
        claudeKeyField.text = s.claudeApiKey
        claudeModelCombo.selectedItem = s.claudeModel
        languageCombo.selectedItem = s.language
    }
}