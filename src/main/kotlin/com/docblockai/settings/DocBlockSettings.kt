package com.docblockai.settings

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
        var openaiApiKey: String = "",
        var openaiModel: String = "gpt-4o",
        var claudeApiKey: String = "",
        var claudeModel: String = "claude-sonnet-4-6",
        var language: String = "English",
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): DocBlockSettings =
            ApplicationManager.getApplication().getService(DocBlockSettings::class.java)
    }
}
