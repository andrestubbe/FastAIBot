package demo;

import fastansi.FastANSI;

public final class UiConsole {

    private UiConsole() {}

    public static String gray(String text) {
        return FastANSI.FG_BRIGHT_BLACK + text + FastANSI.RESET;
    }

    public static String white(String text) {
        return FastANSI.FG_BRIGHT_WHITE + text + FastANSI.RESET;
    }

    public static void printUserPrompt() {
        System.out.print(gray(AppConfig.USER_PREFIX));
    }

    public static void printAiPrompt() {
        System.out.println();
        System.out.print(gray(AppConfig.AI_PREFIX));
    }

    public static void printMetricsSummary(int totalTokens, long durationMs) {
        System.out.println("\n" + AppConfig.INDENT + gray(String.format("(Tokens used: %d | Time: %d ms)", totalTokens, durationMs)));
        System.out.println();
    }

    public static void printExitMessage() {
        System.out.println("\n" + AppConfig.INDENT + white("Exiting the current session. If you need further assistance, let me know!"));
        System.out.println(AppConfig.INDENT + "😊\n");
    }
}
