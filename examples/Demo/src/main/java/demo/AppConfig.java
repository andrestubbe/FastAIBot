package demo;

public final class AppConfig {
    public static final String DEFAULT_MODEL = "llama:smollm2:1.7b?gpu=33";
    public static final String SYSTEM_PROMPT = "Du bist ein hilfreicher Assistent.";
    
    public static final int MARGIN = 8;
    public static final int MAX_COLS = 80;
    public static final String INDENT = "        ";

    public static final String USER_PREFIX = "User:   ";
    public static final String AI_PREFIX   = "AI:     ";

    private AppConfig() {}
}
