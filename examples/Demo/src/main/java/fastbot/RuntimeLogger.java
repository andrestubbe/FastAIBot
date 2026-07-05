package fastbot;

public class RuntimeLogger {
    public static void log(String msg) {
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream("C:\\Users\\andre\\fastbot_runtime.log", true);
            java.io.PrintStream ps = new java.io.PrintStream(fos);
            ps.println("[" + new java.util.Date() + "] " + msg);
            ps.close();
        } catch (Exception ignored) {
        }
    }
}
