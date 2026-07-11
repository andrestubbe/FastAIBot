package fastaibot;

import fastai.AI;
import fastaimemory.ConversationHistory;
import fastaimemory.MemoryContextBuilder;
import fastaimemory.PlainTextFormatter;
import java.util.function.Consumer;

/**
 * The core orchestrator for FastAIBot.
 * It manages the session memory, sends prompts to the FastAI client,
 * and routes the streaming output through the FastBotOutputMixer.
 */
public class FastAIBot {
    
    private final AI ai;
    private final ConversationHistory history;
    private final MemoryContextBuilder contextBuilder;
    private final FastBotOutputMixer outputMixer;

    public FastAIBot(AI ai, String systemPrompt, Consumer<String> textOutput, Consumer<String> actionOutput) {
        this.ai = ai;
        this.history = new ConversationHistory();
        this.contextBuilder = new MemoryContextBuilder(new PlainTextFormatter());
        this.outputMixer = new FastBotOutputMixer(textOutput, actionOutput);
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            this.history.system(systemPrompt);
        }
    }

    /**
     * Streams the chat interaction, parsing actions and text simultaneously.
     */
    public void streamChat(String userInput) {
        // 1. Add user input to memory
        history.user(userInput);

        // 2. Build the context for the AI
        String promptContext = contextBuilder.build(history);

        // 3. Prepare to capture the full response for memory while streaming
        StringBuilder fullResponse = new StringBuilder();
        
        Consumer<String> streamInterceptor = token -> {
            fullResponse.append(token);
            outputMixer.accept(token); // Route to the mixer instantly
        };

        // 4. Query the AI natively via streaming
        ai.stream(promptContext, streamInterceptor);

        // 5. Add the final AI response to memory
        history.assistant(fullResponse.toString());
    }
    
    public ConversationHistory getHistory() {
        return history;
    }
}
