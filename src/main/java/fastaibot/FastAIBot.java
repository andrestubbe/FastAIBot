package fastaibot;

import fastai.AI;
import fastaimemory.ConversationHistory;
import fastaimemory.MemoryContextBuilder;
import fastaimemory.MemoryFormatter;
import fastaimemory.PlainTextFormatter;

import java.util.function.Consumer;

/**
 * The high-performance conversational bot orchestrator for the FastJava ecosystem.
 * <p>
 * {@code FastAIBot} acts as the central link between LLM inference execution ({@link AI})
 * and conversational context management ({@link ConversationHistory} and {@link MemoryContextBuilder}).
 * It provides zero-latency token-by-token streaming responses while automatically maintaining stateful
 * multi-turn conversation memory.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * AI ai = FastAI.connect("ollama:llama3.2:1b");
 * Consumer<String> streamOutput = token -> System.out.print(token);
 * 
 * FastAIBot bot = new FastAIBot(ai, "Du bist ein hilfreicher Assistent.", streamOutput);
 * bot.streamChat("Hallo!");
 * }</pre>
 *
 * @author Andre Stubbe
 * @version 0.1.1
 * @see AI
 * @see ConversationHistory
 * @see MemoryContextBuilder
 */
public final class FastAIBot {

    private final AI ai;
    private final ConversationHistory history;
    private final MemoryContextBuilder contextBuilder;
    private final Consumer<String> textOutput;

    /**
     * Constructs a new {@code FastAIBot} instance using the default {@link PlainTextFormatter}.
     *
     * @param ai           the connected FastAI client used for streaming inference
     * @param systemPrompt the initial system instruction to set the bot's persona (may be {@code null})
     * @param textOutput   the consumer that receives each generated text token in real-time
     */
    public FastAIBot(AI ai, String systemPrompt, Consumer<String> textOutput) {
        this(ai, systemPrompt, textOutput, new PlainTextFormatter());
    }

    /**
     * Constructs a new {@code FastAIBot} instance with a custom {@link MemoryFormatter}.
     *
     * @param ai           the connected FastAI client used for streaming inference
     * @param systemPrompt the initial system instruction to set the bot's persona (may be {@code null})
     * @param textOutput   the consumer that receives each generated text token in real-time
     * @param formatter    the memory formatter used by {@link MemoryContextBuilder} to structure prompts
     */
    public FastAIBot(AI ai, String systemPrompt, Consumer<String> textOutput, MemoryFormatter formatter) {
        this.ai = ai;
        this.history = new ConversationHistory();
        this.contextBuilder = new MemoryContextBuilder(formatter);
        this.textOutput = textOutput;

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            this.history.system(systemPrompt);
        }
    }

    /**
     * Executes a single chat interaction turn asynchronously using real-time token streaming.
     * <p>
     * This method appends the user input to the active {@link ConversationHistory}, builds the complete
     * context prompt via {@link MemoryContextBuilder}, streams the AI's response tokens directly to the
     * configured {@code textOutput} consumer, and records the full assistant response in conversation memory.
     * </p>
     *
     * @param userInput the text prompt or question submitted by the user
     */
    public void streamChat(final String userInput) {
        // 1. Add user input to memory
        this.history.user(userInput);

        // 2. Build the context for the AI
        final String promptContext = this.contextBuilder.build(this.history);

        // 3. Prepare to capture the full response for memory while streaming
        final StringBuilder fullResponse = new StringBuilder();

        final Consumer<String> streamInterceptor = token -> {
            fullResponse.append(token);
            if (this.textOutput != null) {
                this.textOutput.accept(token);
            }
        };

        // 4. Query the AI natively via streaming
        this.ai.stream(promptContext, streamInterceptor);

        // 5. Add the final AI response to memory
        this.history.assistant(fullResponse.toString());
    }

    public ConversationHistory getHistory() {
        return this.history;
    }
}
