package fastbot.scene;

import fastai.AI;
import fastai.FastAI;
import fastbot.FastBot;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AIController {

    private final ExecutorService aiExecutor;
    private final AtomicReference<AI> brainRef = new AtomicReference<>(null);
    private final AtomicReference<String> systemPromptRef = new AtomicReference<>("");
    private final AtomicBoolean aiReady = new AtomicBoolean(false);
    private volatile boolean ollamaMissing = false;

    public AIController() {
        this.aiExecutor = Executors.newSingleThreadExecutor();
    }

    private final AtomicReference<FastBot> botRef = new AtomicReference<>(null);

    public void start() {
        aiExecutor.submit(() -> {
            try {
                AI brain = FastAI.connect("ollama:llama3.2:1b", "");
                brainRef.set(brain);
                String systemPrompt = Files.readString(Paths.get("C:\\Users\\andre\\Documents\\2026-06-14-Work-FastJava\\FastBot\\prompts\\alternative_linkedin_prompt.txt"));
                systemPromptRef.set(systemPrompt);

                FastBot bot = new FastBot(brain, systemPrompt, token -> {}, action -> {});
                botRef.set(bot);

                // Force actual VRAM load by sending a short dummy request
                bot.streamChat("Pre-warm");

                aiReady.set(true);
            } catch (Throwable t) {
                if (t.getMessage() != null && (t.getMessage().contains("ConnectException") || t.getMessage().contains("Connection refused"))) {
                    ollamaMissing = true;
                }
            }
        });
    }

    public void submitTask(Runnable task) {
        aiExecutor.submit(task);
    }

    public String generateResponseSync(String messageToSend) throws Exception {
        int waitCycles = 0;
        while (!aiReady.get() && waitCycles < 150) {
            Thread.sleep(100);
            waitCycles++;
        }

        if (ollamaMissing) {
            throw new Exception("Connection refused - Ollama may be missing or down.");
        }

        FastBot activeBot = botRef.get();
        if (activeBot != null) {
            // streamChat is blocking - collect tokens into result via a new bot with text collector
            StringBuilder result = new StringBuilder();
            AI brain = brainRef.get();
            String systemPrompt = systemPromptRef.get();
            FastBot tempBot = new FastBot(brain, systemPrompt, token -> result.append(token), action -> {});
            tempBot.streamChat(messageToSend);
            return result.toString();
        }
        
        return "";
    }


    public boolean isOllamaMissing() {
        return ollamaMissing;
    }

    public void shutdown() {
        aiExecutor.shutdownNow();
    }
}
