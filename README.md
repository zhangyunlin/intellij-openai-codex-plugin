OpenAI Assistant IntelliJ Plugin (Minimal)

What it does
- Adds a Tools menu action “Ask OpenAI (Selection)” to send a prompt (and optional selected code) to OpenAI and insert/copy the result.
- Provides a right-side Tool Window “OpenAI Assistant” with a simple prompt box and output area.

Requirements
- JDK 17
- IntelliJ IDEA 2024.1+
- Environment variable `OPENAI_API_KEY` set to your API key
- Optional: `OPENAI_MODEL` (default: `gpt-4o-mini`) and `OPENAI_BASE_URL` if using a compatible proxy

Getting started
1) Open this folder in IntelliJ IDEA.
2) Ensure the Gradle JVM uses JDK 17.
3) Run Gradle task: `runIde` (Gradle tool window → Tasks → intellij → runIde).
4) A sandbox IDE launches with the plugin installed.

Usage
- Tools → OpenAI Assistant → Ask OpenAI (Selection)
  - With a selection: describe the transformation or request.
  - Without selection: ask for code generation or explanation.
  - Choose to Insert (into editor) or Copy.
- Tool Window: OpenAI Assistant (right side) for quick prompts.

Notes
- This sample uses the Chat Completions API and the `gpt-4o-mini` model. Codex models are deprecated; use current GPT models for code.
- For production, consider adding a Settings page and storing the API key via IntelliJ’s Credential Store instead of env vars.

