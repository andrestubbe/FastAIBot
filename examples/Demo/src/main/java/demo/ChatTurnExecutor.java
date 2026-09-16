package demo;

import fastaibot.FastAIBot;

import java.util.concurrent.atomic.AtomicInteger;

public class ChatTurnExecutor {

    private final FastAIBot bot;
    private final AtomicInteger tokenCounter;

    public ChatTurnExecutor(final FastAIBot bot, final AtomicInteger tokenCounter) {
        this.bot = bot;
        this.tokenCounter = tokenCounter;
    }

    public void executeTurn(String userQuery) {
        UiConsole.printAiPrompt();
        final long qStart = System.currentTimeMillis();
        this.tokenCounter.set(0);
        this.bot.streamChat(userQuery);
        System.out.print(fastansi.FastANSI.RESET);
        final long duration = System.currentTimeMillis() - qStart;
        final int totalTokens = this.tokenCounter.get();
        UiConsole.printMetricsSummary(totalTokens, duration);
    }
}
