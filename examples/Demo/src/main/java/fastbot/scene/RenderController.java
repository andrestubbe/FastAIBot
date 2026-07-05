package fastbot.scene;

import fastansi.FastANSI;
import fastdwm.FastDWM;
import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import fastaudioprocess.FastAudioProcess;

import java.io.FileWriter;
import java.io.PrintWriter;

public class RenderController {
    
    private int cols;
    private int rows;
    
    private FastTerminalRenderer termRenderer;
    private FastTerminalScene canvas;
    private Scene1Background background;
    private Scene2ChatUI chatUI;
    
    private final BotAudioController audioController;
    private ConversationController conversationController;
    
    private volatile boolean running = true;
    
    public RenderController(int initialCols, int initialRows, BotAudioController audioController) {
        this.cols = initialCols;
        this.rows = initialRows;
        this.audioController = audioController;
        
        try {
            int[] sz = FastTerminal.getWindowSize(cols, rows);
            if (sz != null && sz[0] > 0 && sz[1] > 0) {
                this.cols = sz[0];
                this.rows = sz[1];
            }
        } catch (Throwable ignored) {}

        this.termRenderer = new FastTerminalRenderer(cols, rows);
        this.canvas = new FastTerminalScene(0, 0, cols, rows);
        this.termRenderer.addScene(canvas);
        this.termRenderer.setDirtyRectanglesEnabled(true);

        this.background = new Scene1Background(8); // SSAA 4
        this.chatUI = new Scene2ChatUI(0, 0, cols, rows);

        this.background.resize(cols, rows);
        
        if (this.audioController.getSpeechPlayer() != null) {
            this.chatUI.setAudioPlayer(this.audioController.getSpeechPlayer());
        }
    }
    
    public void setConversationController(ConversationController conversationController) {
        this.conversationController = conversationController;
    }

    public Scene2ChatUI getChatUI() {
        return chatUI;
    }
    
    public BotHead getBotHead() {
        return background.getBotHead();
    }
    
    public void stop() {
        this.running = false;
    }
    
    public void startLoop() {
        final long TARGET_MS = 1000 / 30;
        long lastFpsUpdateTime = System.currentTimeMillis();
        int fpsFrameCount = 0;
        double realFps = 60.0;
        
        FastDWM.beginTimerPeriod(1);

        try {
            while (running) {
                long loopStart = System.nanoTime();

                int[] sz = FastTerminal.getWindowSize(cols, rows);
                if (termRenderer.resize(sz[0], sz[1])) {
                    cols = sz[0];
                    rows = sz[1];
                    canvas.resize(cols, rows);
                    chatUI.resize(cols, rows);
                    synchronized (this) {
                        background.resize(cols, rows);
                    }
                }

                try {
                    // Focus tracking logic for BotHead
                    BotHead head = background.getBotHead();
                    if (head != null) {
                        // Update user typing state (gaze direction)
                        boolean typing = false;
                        if (conversationController != null) {
                            long timeSinceType = System.currentTimeMillis() - conversationController.getLastUserTypeTime();
                            if (timeSinceType < 1500) {
                                typing = true;
                            }
                        }
                        if (chatUI.isBotTyping()) {
                            typing = false;
                        }

                        // Only set user typing if we are NOT performing a gesture to avoid conflicts
                        if (!head.isPerformingGesture()) {
                            head.setUserIsTyping(typing);
                        }

                        byte[] audioBuf = audioController.getActiveAudioData();
                        if (head != null && audioController.getSpeechPlayer() != null && audioController.getSpeechPlayer().isPlaying() && audioBuf != null) {
                            long posMs = audioController.getSpeechPlayer().getPosition();
                            // 44100 Hz, 16-bit (2 bytes), Stereo (2 channels) = 176400 bytes per second = 176 bytes per millisecond
                            int byteOffset = (int) (posMs * 176);

                            // Look at a 40ms window (7040 bytes)
                            int windowSize = 7040;
                            if (byteOffset >= 0 && byteOffset + windowSize <= audioBuf.length) {
                                float rms = FastAudioProcess.computeRms(audioBuf, byteOffset, windowSize);

                                // Scale RMS to openness (typically RMS peaks around 0.1 - 0.25)
                                float openness = Math.min(1.0f, rms * 7.0f);
                                head.setMouthOpenness(openness);
                            } else {
                                head.setMouthOpenness(0.0f);
                            }
                        } else if (head != null) {
                            head.setMouthOpenness(0.0f); // Keep closed if silent
                        }
                    }
                } catch (Throwable e) {
                    // Ignore lipsync errors
                }

                fpsFrameCount++;
                long nowMs = System.currentTimeMillis();
                if (nowMs - lastFpsUpdateTime >= 1000) {
                    realFps = (fpsFrameCount * 1000.0) / (nowMs - lastFpsUpdateTime);
                    fpsFrameCount = 0;
                    lastFpsUpdateTime = nowMs;
                }

                synchronized (this) {
                    background.update(TARGET_MS);
                    // 1. Render Scene 1 (3D background with half-blocks)
                    background.render(canvas);

                    // 2. Render Scene 2 (Chat UI overlay with full-blocks and text)
                    chatUI.render(canvas);

                    // 3. Render Scene 3 (Debug Overlay for Gestures)
                    BotHead head = background.getBotHead();
                    String lastGesture = head != null ? head.getLastGestureName() : "";
                    if (lastGesture != null && !lastGesture.isEmpty()) {
                        String debugText = "GESTURE: [" + lastGesture + "]";
                        for (int i = 0; i < debugText.length(); i++) {
                            canvas.writeCell(1 + i, 1, debugText.charAt(i), 0xFFFFFF, 0x000000);
                        }
                    }
                }

                FastTerminal.setTitle(String.format("FPS: %d | AI Chat Mode | SSAA: 16x", (int) Math.round(realFps)));
                termRenderer.render();

                long elapsed = (System.nanoTime() - loopStart) / 1_000_000L;
                if (elapsed < TARGET_MS) {
                    try {
                        Thread.sleep(TARGET_MS - elapsed);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } catch (Throwable t) {
            try (PrintWriter pw = new PrintWriter(new FileWriter("crash.log"))) {
                t.printStackTrace(pw);
            } catch (Exception ex) {
            }
            this.running = false;
        } finally {
            FastDWM.endTimerPeriod(1);
        }
    }
}
