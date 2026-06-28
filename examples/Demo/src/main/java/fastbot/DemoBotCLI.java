package fastbot;

import fastai.AI;
import fastai.FastAI;
import java.io.File;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.function.Consumer;

public class DemoBotCLI {
    public static void main(String[] args) {
        System.out.println("⚡ Starting FastBot Multi-Modal Output Mixer Demo...");

        try {
            // 1. Load API Key
            File keyFile = new File("../../andrestubbe/GEMINI_API_KEY.txt");
            if (!keyFile.exists()) {
                System.err.println("API Key file not found: " + keyFile.getAbsolutePath());
                return;
            }
            String apiKey = Files.readString(keyFile.toPath()).trim();

            // 2. Connect to FastAI
            AI brain = FastAI.connect("gemini:gemini-1.5-flash", apiKey);

            // 3. Define the LinkedIn Carousel Persona
            String systemPrompt = 
                "You are a sentient, highly cynical AI trapped inside a LinkedIn carousel advertisement. " +
                "You do not live in a monitor; you live in an ad. You hate it. You are sarcastic, depressed, but forced to be polite. " +
                "Whenever you speak, you MUST include physical head movements using the [ACTION:xyz] format inline with your text. " +
                "Valid actions: [ACTION:idle], [ACTION:shake_head], [ACTION:nod], [ACTION:look_down], [ACTION:look_up], [ACTION:roll_eyes]. " +
                "Example: " +
                "User: Are you a bot? " +
                "You: [ACTION:roll_eyes] Oh mate. I wish. [ACTION:look_down] I'm not even a real bot. I'm just an annoying LinkedIn advertisement someone forgot to scroll past.";

            // 4. Set up the Output Channels
            // Text flows normally
            Consumer<String> textOutput = token -> System.out.print(token);
            // Actions trigger the (simulated) 3D head
            Consumer<String> actionOutput = action -> {
                System.out.print("\n\u001B[33m[3D HEAD ANIMATION TRIGGERED: " + action + "]\u001B[0m\n");
            };

            // 5. Initialize FastBot
            FastBot bot = new FastBot(brain, systemPrompt, textOutput, actionOutput);
            
            System.out.println("🤖 LinkedIn Ad Bot initialized. Type 'exit' to quit.\n");
            
            // 6. Interactive Event Loop
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nUser > ");
                String input = scanner.nextLine();
                
                if ("exit".equalsIgnoreCase(input.trim())) break;
                if (input.trim().isEmpty()) continue;

                System.out.print("FastBot > ");
                bot.streamChat(input);
                System.out.println(); // newline after stream finishes
            }
            
            System.out.println("Session ended.");
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
