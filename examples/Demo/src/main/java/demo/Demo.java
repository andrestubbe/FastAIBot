package demo;

import fastai.AI;
import fastai.FastAI;
import fastaibot.FastAIBot;
import fastaimemory.ChatMLFormatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Demo {

    public static void main(String[] args) {
        new Demo().runInteractive();
    }

    public void runInteractive() {
        try {
            final AI ai = FastAI.connect(AppConfig.DEFAULT_MODEL);
            final AtomicInteger tokenCounter = new AtomicInteger(0);

            final Consumer<String> textOutput = StreamFormatterUtil.createIndentedStreamConsumer(
                    AppConfig.MARGIN,
                    AppConfig.MAX_COLS,
                    tokenCounter
            );

            final FastAIBot bot = new FastAIBot(
                    ai,
                    AppConfig.SYSTEM_PROMPT,
                    textOutput,
                    new ChatMLFormatter()
            );

            final ChatTurnExecutor executor = new ChatTurnExecutor(bot, tokenCounter);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                UiConsole.printUserPrompt();
                String userQuery = reader.readLine();

                if (isExitCommand(userQuery)) {
                    UiConsole.printExitMessage();
                    break;
                }

                if (userQuery == null || userQuery.trim().isEmpty()) continue;

                executor.executeTurn(userQuery);
            }

        } catch (Exception e) {
            System.err.println("Error running Demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isExitCommand(String query) {
        if (query == null) return true;
        String trimmed = query.trim();
        return trimmed.equalsIgnoreCase("exit") || trimmed.equalsIgnoreCase("quit");
    }
}
