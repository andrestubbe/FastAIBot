package fastbot.scene;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.lang.reflect.Field;

import fastterminal.FastTerminalScene;
import fastaudio.FastAudioPlayer;

public class Scene2ChatUI {

    private int x;
    private int y;
    private int width;
    private int height;

    private ChatMessage cachedPreloadMsg1 = new ChatMessage("\u25CF", false);
    private ChatMessage cachedPreloadMsg2 = new ChatMessage("\u25CB", false);
    private ChatMessage cachedDraftMsg = new ChatMessage("", true);

    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private final List<ChatMessage> renderMessages = new ArrayList<>();
    private String draftMessage = "";

    private static Field fgFieldCached;
    private static Field bgFieldCached;

    static {
        try {
            fgFieldCached = FastTerminalScene.class.getDeclaredField("fgBuffer");
            bgFieldCached = FastTerminalScene.class.getDeclaredField("bgBuffer");
            fgFieldCached.setAccessible(true);
            bgFieldCached.setAccessible(true);
        } catch (Exception e) {
            // ignore
        }
    }

    private boolean botIsTyping = false;

    public Scene2ChatUI(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void addMessage(String text, boolean isUser) {
        messages.add(new ChatMessage(text, isUser));
    }

    public synchronized void appendToLastBotMessage(String token) {
        if (!messages.isEmpty() && !messages.get(messages.size() - 1).isUser) {
            messages.get(messages.size() - 1).append(token);
        } else {
            addMessage(token, false);
        }
    }

    public synchronized ChatMessage appendToLastBotMessageTypewriter(String token) {
        if (!messages.isEmpty() && !messages.get(messages.size() - 1).isUser) {
            ChatMessage msg = messages.get(messages.size() - 1);
            msg.appendTypewriter(token);
            return msg;
        } else {
            ChatMessage msg = new ChatMessage("", false);
            msg.appendTypewriter(token);
            messages.add(msg);
            return msg;
        }
    }

    public void setDraftMessage(String text) {
        this.draftMessage = text;
    }

    public String getDraftMessage() {
        return this.draftMessage;
    }

    public void setBotIsTyping(boolean typing) {
        this.botIsTyping = typing;
    }

    public boolean isBotTyping() {
        return this.botIsTyping;
    }

    private FastAudioPlayer player;

    public void setAudioPlayer(FastAudioPlayer player) {
        this.player = player;
    }

    public void render(FastTerminalScene canvas) {
        int currentY = height - 1; // Start from bottom

        renderMessages.clear();
        for (ChatMessage msg : messages) {
            msg.updateTypewriter(this.player);
            renderMessages.add(msg);
        }

        ChatMessage preloadMsg = null;
        // Add an animated preload indicator if the bot is thinking OR if the bot message is added but typewriter hasn't started yet
        boolean isWaitingForBotStart = false;
        if (!messages.isEmpty()) {
            ChatMessage lastMsg = messages.get(messages.size() - 1);
            if (lastMsg.isUser) {
                isWaitingForBotStart = botIsTyping;
            } else if (!lastMsg.typewriterStarted) {
                isWaitingForBotStart = true;
            }
        } else {
            isWaitingForBotStart = botIsTyping;
        }

        if (isWaitingForBotStart) {
            preloadMsg = (System.currentTimeMillis() / 500) % 2 == 0 ? cachedPreloadMsg1 : cachedPreloadMsg2;
            renderMessages.add(preloadMsg);
        }

        if (draftMessage != null && (!draftMessage.isEmpty() || !botIsTyping)) {
            // Use non-breaking space (\u00A0) when off or when bot is typing, so wrapText's split(" ") doesn't delete it
            String cursor = (botIsTyping || (System.currentTimeMillis() / 500) % 2 != 0) ? "\u00A0" : "|";
            cachedDraftMsg.text = draftMessage + cursor;
            cachedDraftMsg.fullTargetText = cachedDraftMsg.text;
            cachedDraftMsg.charsRevealed = cachedDraftMsg.fullTargetText.length();
            cachedDraftMsg.cachedLines = null; // force rewrap
            renderMessages.add(cachedDraftMsg);
        }

        // Render backwards so newest message is at bottom
        for (int i = renderMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = renderMessages.get(i);

            // Skip rendering if typewriter is enabled but not yet started (prevents empty box drawing)
            if (!msg.isUser && !msg.typewriterStarted && msg != preloadMsg) {
                continue;
            }

            // Calculate wrapped lines statically based on the FULL text so bubble layout is stable
            // and we don't allocate strings every frame.
            java.util.List<String> lines = msg.cachedLines;
            if (lines == null) {
                lines = wrapText(msg.fullTargetText, 36);
                msg.cachedLines = lines;
            }

            // Tightly fit bubble width to the longest line (or at least 1)
            int contentWidth = 1;
            for (String l : lines) {
                if (l.length() > contentWidth) contentWidth = l.length();
            }

            // Determine how many lines are actually visible based on charsRevealed
            int visibleLinesCount = 0;
            int countedChars = 0;
            int textLimit = msg.charsRevealed;
            
            for (String l : lines) {
                visibleLinesCount++;
                countedChars += l.length();
                if (countedChars >= textLimit) {
                    break;
                }
            }
            if (visibleLinesCount == 0 && !lines.isEmpty()) visibleLinesCount = 1;

            int bubbleHeight = visibleLinesCount + 2; // +2 for top/bottom padding
            if (msg == preloadMsg) {
                bubbleHeight = 1; // Just one character high, no padding
            }

            currentY -= bubbleHeight;

            if (currentY + bubbleHeight <= y) {
                break; // Stop rendering if the ENTIRE bubble is off the top of the window
            }

            int bubbleY = currentY + (msg == preloadMsg ? 0 : 1);
            int bubbleColor = msg.isUser ? 0x07070F : 0xF59E0B;
            int fgColor = msg.isUser ? 0xF59E0B : 0x07070F;

            int bubbleWidth = contentWidth + 4;

            int bubbleX;
            if (msg.isUser) {
                // User bubble is anchored to the right margin (3 chars)
                bubbleX = (x + width) - bubbleWidth - 3;
            } else {
                // Bot bubble always starts at a fixed column (max width 36 + padding 4 + margin 8) from the right
                bubbleX = (x + width) - 40 - 8;
            }

            if (msg == preloadMsg) {
                // Draw just the blinking circle without background, using yellow color
                canvas.writeCell(bubbleX, currentY, msg.text.charAt(0), 0xF59E0B, -1);
                continue; // Skip the rest of the bubble rendering
            }

            int[] sceneFg = null;
            int[] sceneBg = null;
            if (fgFieldCached != null && bgFieldCached != null) {
                try {
                    sceneFg = (int[]) fgFieldCached.get(canvas);
                    sceneBg = (int[]) bgFieldCached.get(canvas);
                } catch (Exception e) {
                    // fallback
                }
            }

            // Draw Top Border (upper half block \u2580)
            // We want the TOP half to be existing scene top color, and BOTTOM half to be bubbleColor.
            for (int c = 0; c < bubbleWidth; c++) {
                int screenX = bubbleX + c;
                if (screenX >= 0 && screenX < canvas.getWidth() && bubbleY >= 0 && bubbleY < canvas.getHeight()) {
                    int existingFg = -1; // Fallback
                    if (sceneFg != null) {
                        int idx = bubbleY * canvas.getWidth() + screenX;
                        if (idx >= 0 && idx < sceneFg.length) existingFg = sceneFg[idx];
                    }
                    if (existingFg == -1) existingFg = 0x000000;
                    canvas.writeCell(screenX, bubbleY, '\u2580', existingFg, bubbleColor);
                }
            }

            // Draw Middle (spaces)
            for (int r = 1; r < bubbleHeight - 1; r++) {
                int screenY = bubbleY + r;
                if (screenY >= 0 && screenY < canvas.getHeight()) {
                    for (int c = 0; c < bubbleWidth; c++) {
                        canvas.writeCell(bubbleX + c, screenY, ' ', fgColor, bubbleColor);
                    }
                }
            }

            // Draw Bottom Border (upper half block \u2580)
            // We want the TOP half to be bubbleColor, and BOTTOM half to be existing scene bottom color.
            for (int c = 0; c < bubbleWidth; c++) {
                int screenX = bubbleX + c;
                int bY = bubbleY + bubbleHeight - 1;
                if (screenX >= 0 && screenX < canvas.getWidth() && bY >= 0 && bY < canvas.getHeight()) {
                    int existingBg = -1; // Fallback
                    if (sceneBg != null) {
                        int idx = bY * canvas.getWidth() + screenX;
                        if (idx >= 0 && idx < sceneBg.length) existingBg = sceneBg[idx];
                    }
                    if (existingBg == -1) existingBg = 0x000000;
                    canvas.writeCell(screenX, bY, '\u2580', bubbleColor, existingBg);
                }
            }

            // Draw Text (Revealed portion only based on charsRevealed)
            int charsPrinted = 0;

            for (int r = 0; r < visibleLinesCount; r++) {
                int screenY = bubbleY + 1 + r;
                if (screenY >= 0 && screenY < canvas.getHeight()) {
                    String line = lines.get(r);
                    for (int c = 0; c < line.length(); c++) {
                        if (charsPrinted < textLimit) {
                            canvas.writeCell(bubbleX + 2 + c, screenY, line.charAt(c), fgColor, bubbleColor);
                            charsPrinted++;
                        }
                    }
                }
            }
        }
    }

    private java.util.List<String> wrapText(String text, int maxWidth) {
        text = text.replace("\r", ""); // Strip carriage returns completely
        String[] explicitLines = text.split("\n");
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String block : explicitLines) {
            if (block.isEmpty()) {
                lines.add("");
                continue;
            }
            String[] words = block.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (currentLine.length() + word.length() + (currentLine.length() > 0 ? 1 : 0) > maxWidth) {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                    // Handle extremely long words
                    while (word.length() > maxWidth) {
                        lines.add(word.substring(0, maxWidth));
                        word = word.substring(maxWidth);
                    }
                }
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

    public static class ChatMessage {
        String text;
        boolean isUser;
        java.util.List<String> cachedLines = null;

        String fullTargetText = "";
        long lastCharRevealTime = 0;
        int charsRevealed = 0;

        volatile int msPerChar = 28;
        volatile boolean typewriterStarted = false;

        ChatMessage(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
            this.fullTargetText = text;
            this.charsRevealed = text.length();
            if (isUser) {
                this.typewriterStarted = true;
            }
        }

        void append(String token) {
            this.text += token;
            this.text = this.text.replace("<|im_end|>", "").replace("<|im_start|>", "");
            this.fullTargetText = this.text;
            this.charsRevealed = this.text.length();
            this.cachedLines = null; // Invalidate cache
        }

        void appendTypewriter(String token) {
            this.fullTargetText += token;
            this.fullTargetText = this.fullTargetText.replace("<|im_end|>", "").replace("<|im_start|>", "");
            this.cachedLines = null;
        }

        public void startTypewriter(int msPerChar) {
            this.msPerChar = msPerChar > 0 ? msPerChar : 28;
            this.typewriterStarted = true;
            this.lastCharRevealTime = System.currentTimeMillis();
            this.charsRevealed = 0;
            this.cachedLines = null;
        }

        void updateTypewriter(FastAudioPlayer player) {
            if (isUser) {
                this.charsRevealed = this.fullTargetText.length();
                this.typewriterStarted = true;
                return;
            }
            if (!typewriterStarted) {
                this.charsRevealed = 0;
                return;
            }

            if (charsRevealed < fullTargetText.length()) {
                if (player != null && player.isPlaying() && !isUser) {
                    long duration = player.getDuration();
                    long pos = player.getPosition();
                    if (duration > 0) {
                        double pct = (double) pos / duration;
                        int targetChars = (int) (fullTargetText.length() * pct);
                        // Prevent index overflows and ensure monotonicity
                        targetChars = Math.max(charsRevealed, Math.min(fullTargetText.length(), targetChars));
                        if (targetChars != charsRevealed) {
                            charsRevealed = targetChars;
                        }
                        return;
                    }
                }

                // Fallback: Time-based reveal
                long now = System.currentTimeMillis();
                if (lastCharRevealTime == 0) lastCharRevealTime = now;

                if (now - lastCharRevealTime > msPerChar) {
                    int charsToAdd = (int) ((now - lastCharRevealTime) / msPerChar);
                    charsRevealed = Math.min(fullTargetText.length(), charsRevealed + charsToAdd);
                    lastCharRevealTime = now;
                }
            }
        }
    }
}
