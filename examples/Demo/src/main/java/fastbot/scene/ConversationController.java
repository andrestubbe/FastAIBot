package fastbot.scene;

import java.util.concurrent.atomic.AtomicLong;

public class ConversationController implements InputController.InputListener {

    public interface AppLifecycleCallback {
        void requestExit(boolean ollamaMissing);
    }

    private final AIController aiController;
    private final BotAudioController audioController;
    private final Scene2ChatUI chatUI;
    private final BotHead botHead;
    private final AppLifecycleCallback appLifecycleCallback;
    
    private final AtomicLong lastUserTypeTime = new AtomicLong(0);

    public ConversationController(AIController aiController, BotAudioController audioController, Scene2ChatUI chatUI, BotHead botHead, AppLifecycleCallback appLifecycleCallback) {
        this.aiController = aiController;
        this.audioController = audioController;
        this.chatUI = chatUI;
        this.botHead = botHead;
        this.appLifecycleCallback = appLifecycleCallback;
    }

    public long getLastUserTypeTime() {
        return lastUserTypeTime.get();
    }

    @Override
    public void onAnyKeyPress(int vKey, String keyChar) {
        if (vKey < 0x70 || vKey > 0x73) {
            lastUserTypeTime.set(System.currentTimeMillis());
        }
    }

    @Override
    public void onEscape() {
        if (appLifecycleCallback != null) {
            appLifecycleCallback.requestExit(false);
        }
    }

    @Override
    public void onGestureKey(String gestureName) {
        botHead.performGesture(gestureName);
    }

    @Override
    public void onPrintableChar(char c) {
        if (chatUI.isBotTyping()) return;
        
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            audioController.playTypingSound();
        });

        String draft = chatUI.getDraftMessage();
        if (draft == null) draft = "";
        chatUI.setDraftMessage(draft + c);
    }

    @Override
    public void onBackspace() {
        if (chatUI.isBotTyping()) return;
        
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            audioController.playTypingSound();
        });

        String draft = chatUI.getDraftMessage();
        if (draft != null && draft.length() > 0) {
            chatUI.setDraftMessage(draft.substring(0, draft.length() - 1));
        }
    }

    @Override
    public void onEnter() {
        if (chatUI.isBotTyping()) return;

        String draft = chatUI.getDraftMessage();
        if (draft != null && !draft.trim().isEmpty()) {
            final String messageToSend = draft;
            chatUI.addMessage(messageToSend, true);
            chatUI.setDraftMessage("");
            chatUI.setBotIsTyping(true);

            aiController.submitTask(() -> {
                try {
                    // User speaks their own message first using UK Alan via Piper
                    audioController.playUserSpeechSync(messageToSend);

                    String fullResponse = aiController.generateResponseSync(messageToSend);

                    String sanitized = fullResponse
                            .replace("’", "'").replace("‘", "'").replace("“", "\"").replace("”", "\"").replace("—", "-").replace("…", "...")
                            .replaceAll("[^\\x00-\\xFF]", "");

                    // Parse secondary instruction tags ([NOD], [SHAKE], [WINK], [LOOK_UP])
                    final boolean triggerNod = sanitized.contains("[NOD]");
                    final boolean triggerShake = sanitized.contains("[SHAKE]");
                    final boolean triggerLookUp = sanitized.contains("[LOOK_UP]");
                    final boolean triggerWink = sanitized.contains("[WINK]");

                    // if (triggerNod) botHead.performGesture("nod");
                    // if (triggerShake) botHead.performGesture("shake");
                    // if (triggerLookUp) botHead.performGesture("look_up");
                    // if (triggerWink) botHead.performGesture("wink");

                    // Clean tags out of spokenText so TTS and typewriter don't render them
                    String spokenText = sanitized
                            .replace("[NOD]", "")
                            .replace("[SHAKE]", "")
                            .replace("[LOOK_UP]", "")
                            .replace("[WINK]", "")
                            .replaceAll("\\[\\s*ACTION:\\s*[^\\]]+\\s*\\]", "")
                            .trim();

                    Scene2ChatUI.ChatMessage botMsg = chatUI.appendToLastBotMessageTypewriter(spokenText);

                    if (!spokenText.isEmpty()) {
                        long duration = audioController.playBotSpeechAndGetDuration(spokenText);
                        int calculatedMsPerChar = 28;
                        if (duration > 0 && spokenText.length() > 0) {
                            calculatedMsPerChar = (int) (duration / spokenText.length());
                            calculatedMsPerChar = Math.max(15, Math.min(80, calculatedMsPerChar));
                        }
                        botMsg.startTypewriter(calculatedMsPerChar);
                    } else {
                        botMsg.startTypewriter(28); // Fallback
                    }
                } catch (Throwable e) {
                    e.printStackTrace();

                    // Check for Ollama connection error
                    if (aiController.isOllamaMissing()) {
                        if (appLifecycleCallback != null) {
                            appLifecycleCallback.requestExit(true);
                        }
                    } else {
                        chatUI.addMessage("ERROR: " + e.getMessage(), false);
                    }
                } finally {
                    chatUI.setBotIsTyping(false);
                }
            });
        }
    }
}
