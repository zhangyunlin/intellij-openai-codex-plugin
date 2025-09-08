package com.example.openai

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

class AIToolWindowFactory : ToolWindowFactory, DumbAware {
    private val logger = Logger.getInstance(AIToolWindowFactory::class.java)

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = SimpleToolWindowPanel(true, true)
        val input = JBTextField()
        input.emptyText.text = "Ask OpenAI..."
        val output = JBTextArea()
        output.isEditable = false

        val sendBtn = JButton("Send")
        sendBtn.addActionListener {
            val prompt = input.text.trim()
            if (prompt.isBlank()) return@addActionListener
            output.text = "Sending..."
            ProgressManager.getInstance().runProcessWithProgressSynchronously({
                try {
                    val client = OpenAIClient(apiKey = null)
                    val res = client.chat(prompt)
                    output.text = res.ifBlank { "(No content returned)" }
                } catch (t: Throwable) {
                    output.text = "Error: ${'$'}{t.message ?: t::class.java.simpleName}"
                    logger.warn("OpenAI error", t)
                }
            }, "Querying OpenAI", false, project)
        }

        val top = JPanel(BorderLayout())
        top.add(input, BorderLayout.CENTER)
        top.add(sendBtn, BorderLayout.EAST)

        panel.setContent(JBScrollPane(output))
        panel.toolbar = top

        val content = ContentFactory.getInstance().createContent(panel, "Chat", false)
        toolWindow.contentManager.addContent(content)
    }
}

