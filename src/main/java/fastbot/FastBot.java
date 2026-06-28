package fastbot;

import fastai.AI;
import fastaimemory.ConversationHistory;
import fastaimemory.MemoryContextBuilder;
import fastaimemory.ChatMLFormatter;

public class FastBot {
    
    private final AI ai;
    private final ConversationHistory history;
    private final MemoryContextBuilder contextBuilder;

    public FastBot(AI ai, String systemPrompt) {
        this.ai = ai;
        this.history = new ConversationHistory();
        this.contextBuilder = new MemoryContextBuilder(new ChatMLFormatter());
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            this.history.system(systemPrompt);
        }
    }

    public String chat(String userInput) {
        // 1. Add user input to memory
        history.user(userInput);

        // 2. Build the context for the AI
        String promptContext = contextBuilder.build(history);

        // 3. Query the AI (using FastAI)
        String aiResponse = ai.ask(promptContext);

        // 4. Add the AI response to memory
        history.assistant(aiResponse);

        return aiResponse;
    }
    
    public ConversationHistory getHistory() {
        return history;
    }
}
