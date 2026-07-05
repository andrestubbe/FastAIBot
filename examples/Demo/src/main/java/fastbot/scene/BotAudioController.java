package fastbot.scene;

import fastaudio.FastAudioPlayer;
import fasttts.FastTTS;
import fasttts.core.FastTTSAudio;
import fasttts.core.FastTTSConfig;
import fasttts.core.FastTTSVoice;
import fasttts.backends.piper.PiperBackend;
import fastaudioprocess.FastAudioProcess;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

public class BotAudioController {

    private final FastAudioPlayer keyPlayer1;
    private final FastAudioPlayer keyPlayer2;
    private final Random rand;
    private final FastTTS tts;
    private final FastAudioPlayer speechPlayer;
    
    private FastTTSVoice botVoice;
    private FastTTSVoice userVoice;

    private byte[] activeAudioData;

    public BotAudioController() {
        this.keyPlayer1 = new FastAudioPlayer();
        this.keyPlayer2 = new FastAudioPlayer();
        this.rand = new Random();
        
        // Load keyboard sounds
        try {
            keyPlayer1.load("C:\\Users\\andre\\Documents\\2026-06-14-Work-FastJava\\FastBot\\examples\\Demo\\assets\\key1.wav");
            keyPlayer2.load("C:\\Users\\andre\\Documents\\2026-06-14-Work-FastJava\\FastBot\\examples\\Demo\\assets\\key2.wav");
        } catch (Throwable t) {
            System.err.println("Failed to load keyboard sounds: " + t.getMessage());
        }

        FastTTS tempTts = null;
        FastAudioPlayer tempPlayer = null;
        try {
            tempTts = new FastTTS();

            tempTts.registerBackend(new PiperBackend(
                "C:\\Users\\andre\\Documents\\2026-06-14-Work-FastJava\\FastBot\\examples\\Demo\\piper.exe",
                "C:\\Users\\andre\\Documents\\2026-06-14-Work-FastJava\\FastBot\\examples\\Demo\\en_US-danny-low.onnx"
            ));

            tempTts.registerBackend(new PiperBackend(
                "C:\\Users\\andre\\Documents\\2026-06-14-Work-FastJava\\FastBot\\examples\\Demo\\piper.exe",
                "C:\\Users\\andre\\Documents\\2026-06-14-Work-FastJava\\FastBot\\examples\\Demo\\en_GB-alan-medium.onnx"
            ));

            tempPlayer = new FastAudioPlayer();
        } catch (Exception e) {
            System.err.println("Error initializing Audio/TTS: " + e.getMessage());
        }

        this.tts = tempTts;
        this.speechPlayer = tempPlayer;

        // Extract voices
        try {
            if (tts != null) {
                List<FastTTSVoice> voices = tts.getAllVoices();
                for (FastTTSVoice v : voices) {
                    if (v.id().contains("danny")) botVoice = v;
                    if (v.id().contains("alan")) userVoice = v;
                }
            }
        } catch (Throwable t) {
            System.err.println("Failed to fetch voices: " + t.getMessage());
        }
    }

    public FastAudioPlayer getSpeechPlayer() {
        return speechPlayer;
    }

    public byte[] getActiveAudioData() {
        return activeAudioData;
    }

    public void playTypingSound() {
        try {
            if (rand.nextBoolean()) {
                keyPlayer1.stop();
                keyPlayer1.play();
            } else {
                keyPlayer2.stop();
                keyPlayer2.play();
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Synthesizes and plays the user's text synchronously.
     * Blocks until the audio finishes playing (or max 5 seconds).
     */
    public void playUserSpeechSync(String text) {
        if (tts == null || userVoice == null || speechPlayer == null) return;
        try {
            FastTTSConfig config = new FastTTSConfig();
            config.setRate(1.15f);
            FastTTSAudio audio = tts.speak("piper", text, userVoice, config);
            
            if (audio != null && audio.getData() != null && audio.getData().length > 0) {
                byte[] resampledUserBytes = fastaudioprocess.FastAudioProcess.resampleWavTo44100(audio.getData());
                this.activeAudioData = null; // Clear mouth buffer for User off-screen voice
                
                File tempWavUser = File.createTempFile("user_out", ".wav");
                tempWavUser.deleteOnExit();
                Files.write(tempWavUser.toPath(), resampledUserBytes);
                
                speechPlayer.stop();
                speechPlayer.load(tempWavUser.getAbsolutePath());
                speechPlayer.play();
                
                long duration = speechPlayer.getDuration();
                if (duration > 0) {
                    Thread.sleep(duration);
                }
            }
        } catch (Throwable t) {
            System.err.println("Failed to play user speech: " + t.getMessage());
        }
    }

    /**
     * Synthesizes and plays the bot's text asynchronously.
     * Populates the activeAudioData buffer for lip-sync.
     * Returns the duration of the audio in milliseconds.
     */
    public long playBotSpeechAndGetDuration(String text) {
        if (tts == null || botVoice == null || speechPlayer == null) return 0;
        try {
            FastTTSConfig config = new FastTTSConfig();
            config.setRate(1.0f);
            FastTTSAudio audio = tts.speak("piper", text, botVoice, config);
            
            if (audio != null && audio.getData() != null && audio.getData().length > 0) {
                byte[] resampledBotBytes = FastAudioProcess.resampleWavTo44100(audio.getData());
                this.activeAudioData = resampledBotBytes;
                
                File tempWavBot = File.createTempFile("bot_out", ".wav");
                tempWavBot.deleteOnExit();
                Files.write(tempWavBot.toPath(), resampledBotBytes);
                
                speechPlayer.stop();
                speechPlayer.load(tempWavBot.getAbsolutePath());
                long duration = speechPlayer.getDuration();
                speechPlayer.play();
                return duration;
            }
        } catch (Throwable t) {
            System.err.println("Failed to play bot speech: " + t.getMessage());
        }
        return 0;
    }
}
