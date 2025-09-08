package com.example.openai

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsContexts

class AskAIAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selection = editor?.selectionModel?.selectedText?.takeIf { it.isNotBlank() }
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val fileContext = buildString {
            if (file != null) {
                append("File: ").append(file.name).append('\n')
                append("Language: ").append(file.language.id).append('\n')
            }
            if (selection != null) {
                append("\nSelected code:\n\n").append(selection).append('\n')
            }
        }.takeIf { it.isNotBlank() }

        val userPrompt = Messages.showInputDialog(
            project,
            promptLabel(selection != null),
            "Ask OpenAI",
            Messages.getQuestionIcon()
        ) ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Querying OpenAI...") {
            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                indicator.isIndeterminate = true
                val prompt = buildString {
                    append(userPrompt.trim())
                    if (fileContext != null) {
                        append("\n\nContext:\n").append(fileContext)
                    }
                }

                val client = OpenAIClient(apiKey = null)
                val response: String = try {
                    client.chat(prompt)
                } catch (t: Throwable) {
                    showError(project, "${'$'}{t.message ?: t::class.java.simpleName}")
                    return
                }

                ApplicationManager.getApplication().invokeLater {
                    val text = response.ifBlank { "(No content returned)" }
                    val choice = Messages.showYesNoCancelDialog(
                        project,
                        text,
                        "OpenAI Response",
                        "Insert",
                        "Copy",
                        "Close",
                        Messages.getInformationIcon()
                    )
                    when (choice) {
                        Messages.YES -> {
                            if (editor != null) {
                                WriteCommandAction.runWriteCommandAction(project) {
                                    val doc = editor.document
                                    val caret = editor.caretModel.currentCaret
                                    val start = editor.selectionModel.selectionStart
                                    val end = editor.selectionModel.selectionEnd
                                    if (start != end) {
                                        doc.replaceString(start, end, text)
                                    } else {
                                        doc.insertString(caret.offset, text)
                                    }
                                }
                            } else showInfo(project, "No editor to insert into.")
                        }
                        Messages.NO -> {
                            val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                            val sel = java.awt.datatransfer.StringSelection(text)
                            clipboard.setContents(sel, sel)
                            showInfo(project, "Copied to clipboard.")
                        }
                        else -> Unit
                    }
                }
            }
        })
    }

    private fun promptLabel(hasSelection: Boolean): @NlsContexts.DialogMessage String =
        if (hasSelection) "Describe what you want with the selected code" else "What do you want to generate or explain?"

    private fun showError(project: Project, msg: String) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(project, msg, "OpenAI Error")
        }
    }

    private fun showInfo(project: Project, msg: String) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showInfoMessage(project, msg, "OpenAI Assistant")
        }
    }
}

