package fastbot;

import java.util.function.Consumer;

/**
 * FastBotOutputMixer intercepts the LLM token stream and splits it into multiple channels.
 * It detects inline action tags like [ACTION:roll_eyes] and routes them to the animation channel,
 * while the raw spoken text flows uninterrupted to the text/TTS channel.
 */
public class FastBotOutputMixer implements Consumer<String> {
    private final Consumer<String> textOutput;
    private final Consumer<String> actionOutput;
    private StringBuilder buffer = new StringBuilder();
    private boolean inAction = false;

    public FastBotOutputMixer(Consumer<String> textOutput, Consumer<String> actionOutput) {
        this.textOutput = textOutput;
        this.actionOutput = actionOutput;
    }

    @Override
    public void accept(String token) {
        // We parse character by character to handle tokens that contain mixed content or partial tags
        for (char c : token.toCharArray()) {
            if (!inAction) {
                if (c == '[') {
                    inAction = true;
                    buffer.setLength(0);
                } else {
                    textOutput.accept(String.valueOf(c));
                }
            } else {
                if (c == ']') {
                    inAction = false;
                    String actionStr = buffer.toString();
                    if (actionStr.startsWith("ACTION:")) {
                        // Route to the animation channel
                        actionOutput.accept(actionStr.substring(7));
                    } else {
                        // It was just brackets, not an action tag. Flush back to text.
                        textOutput.accept("[" + actionStr + "]");
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append(c);
                }
            }
        }
    }
}
