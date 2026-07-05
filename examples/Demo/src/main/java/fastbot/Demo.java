package fastbot;

import fastansi.FastANSI;
import fastbot.scene.BotAudioController;
import fastbot.scene.ConversationController;
import fastbot.scene.InputController;
import fastbot.scene.RenderController;
import fastterminal.FastTerminal;

public class Demo {

    private static volatile boolean ollamaMissing = false;
    private static int cols = 120;
    private static int rows = 30;

    private static final fastbot.scene.AIController aiController = new fastbot.scene.AIController();
    private static fastbot.scene.InputController inputController;
    private static fastbot.scene.RenderController renderController;

    public static void main(String[] args) {
        // Pre-warm the FastAI connection asynchronously at the very beginning
        aiController.start();

        System.out.print(FastANSI.ALT_BUFFER_ON + FastANSI.CURSOR_HIDE + FastANSI.CLEAR_SCREEN);
        System.out.flush();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (renderController != null) renderController.stop();
            System.out.print(FastANSI.RESET + FastANSI.CURSOR_SHOW + FastANSI.ALT_BUFFER_OFF);
            System.out.flush();
            try {
                if (inputController != null) inputController.stopListening();
            } catch (Throwable ignored) {
            }
            try {
                FastTerminal.setRawMode(false);
                FastTerminal.setAnsiRawMode(false);
            } catch (Throwable ignored) {
            }
            aiController.shutdown();
        }));

        final BotAudioController audioController = new BotAudioController();

        renderController = new fastbot.scene.RenderController(cols, rows, audioController);

        fastbot.scene.ConversationController conversationController = new fastbot.scene.ConversationController(
                aiController, audioController, renderController.getChatUI(), renderController.getBotHead(), ollamaMissingResult -> {
            if (ollamaMissingResult) {
                ollamaMissing = true;
            }
            if (renderController != null) renderController.stop();
        });
        
        renderController.setConversationController(conversationController);

        inputController = new fastbot.scene.InputController();
        inputController.startListening(conversationController);

        // This will block and run the loop
        renderController.startLoop();

        System.out.print(FastANSI.RESET + FastANSI.CURSOR_SHOW + FastANSI.ALT_BUFFER_OFF);
        System.out.flush();
        try {
            if (inputController != null) inputController.stopListening();
        } catch (Throwable ignored) {
        }
        try {
            FastTerminal.setRawMode(false);
            FastTerminal.setAnsiRawMode(false);
        } catch (Throwable ignored) {
        }

        if (ollamaMissing) {
            System.err.println("\n\n=======================================================");
            System.err.println(" ERROR: Ollama is missing or not responding on port 11434");
            System.err.println(" Please start Ollama before running this Demo!");
            System.err.println("=======================================================\n\n");
        }

        System.exit(0);
    }
}
