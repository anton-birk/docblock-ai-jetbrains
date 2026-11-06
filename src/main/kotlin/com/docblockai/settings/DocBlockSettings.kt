package com.docblockai.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "DocBlockAISettings", storages = [Storage("DocBlockAI.xml")])
@Service(Service.Level.APP)
class DocBlockSettings : PersistentStateComponent<DocBlockSettings.State> {

    data class State(
        var provider: String = "openai",
        var openaiModel: String = "gpt-6.1-terra",
        var claudeModel: String = "claude-sonnet-5-5",
        var language: String = "English",
        // Legacy plain-text keys from 1.0.0; migrated to PasswordSafe on load.
        var openaiApiKey: String = "",
        var claudeApiKey: String = "",
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        if (state.openaiApiKey.isNotBlank()) {
            openaiApiKey = state.openaiApiKey
            state.openaiApiKey = ""
        }
        if (state.claudeApiKey.isNotBlank()) {
            claudeApiKey = state.claudeApiKey
            state.claudeApiKey = ""
        }
        // gpt-4-turbo / gpt-3.5-turbo are shut down and gpt-4o is being retired; fall back to the default.
        if (state.openaiModel !in OPENAI_MODELS) {
            state.openaiModel = State().openaiModel
        }
        myState = state
    }

    var openaiApiKey: String
        get() = readKey(OPENAI_KEY)
        set(value) = writeKey(OPENAI_KEY, value)

    var claudeApiKey: String
        get() = readKey(CLAUDE_KEY)
        set(value) = writeKey(CLAUDE_KEY, value)

    private fun readKey(attributes: CredentialAttributes): String =
        PasswordSafe.instance.getPassword(attributes).orEmpty()

    private fun writeKey(attributes: CredentialAttributes, value: String) =
        PasswordSafe.instance.setPassword(attributes, value.ifBlank { null })

    companion object {
        val OPENAI_MODELS = listOf("gpt-6.1-terra", "gpt-6.1-sol", "gpt-5.5", "gpt-5.4-mini")

        private val OPENAI_KEY = CredentialAttributes(generateServiceName("DocBlock AI", "OpenAI API key"))
        private val CLAUDE_KEY = CredentialAttributes(generateServiceName("DocBlock AI", "Anthropic API key"))

        fun getInstance(): DocBlockSettings =
            ApplicationManager.getApplication().getService(DocBlockSettings::class.java)
    }
}
