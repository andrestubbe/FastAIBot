package demo;

import fastansi.FastANSI;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class StreamFormatterUtil {

    public static Consumer<String> createIndentedStreamConsumer(
            int margin, 
            int maxCols, 
            AtomicInteger tokenCounter
    ) {
        String indent = " ".repeat(margin);
        int[] col = new int[]{margin};

        return token -> {
            tokenCounter.incrementAndGet();
            for (int i = 0; i < token.length(); i++) {
                char c = token.charAt(i);
                if (c == '\n') {
                    System.out.print("\n" + indent);
                    col[0] = margin;
                } else {
                    if (col[0] >= maxCols && (c == ' ' || c == '\t')) {
                        System.out.print("\n" + indent);
                        col[0] = margin;
                    } else {
                        System.out.print(FastANSI.FG_BRIGHT_WHITE + String.valueOf(c) + FastANSI.RESET);
                        col[0]++;
                    }
                }
            }
            System.out.flush();
        };
    }
}
