# FastBot Reference

This document outlines the API contracts and architectural patterns for the FastBot orchestration runtime.

## Classes

### `FastBot`
The core orchestration instance.

- `public FastBot(AI ai, String systemPrompt, Consumer<String> textOutput, Consumer<String> actionOutput)`
  Initializes the orchestrator, binding the LLM brain to the specific output channels.
  
- `public void streamChat(String userInput)`
  Executes a non-blocking stream interaction. Text flows to `textOutput`, while intercepted actions flow to `actionOutput`.

### `FastBotOutputMixer`
An implementation of `Consumer<String>` that splits LLM streams.
- It parses inline tags formatted as `[ACTION:xyz]`.
- Strips the tags from the raw text stream.
- Routes the action string directly to the designated action channel.

### `FastBotSessionManager` *(Planned)*
Handles concurrency for multiple users connecting to the same Bot instance.

### `FastBotToolBridge` *(Planned)*
Maps `@Tool` annotated Java functions to LLM function-calling specs.
