package fastaibot.benchmark;

import fastai.AI;
import fastai.FastAI;
import fastaibot.FastAIBot;
import fastaimemory.ConversationHistory;
import fastaimemory.MemoryContextBuilder;
import fastaimemory.PlainTextFormatter;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * JMH Microbenchmark Suite for FastAIBot.
 *
 * Measures throughput and allocation footprint of:
 * - Bot instantiation and memory context setup
 * - FastAIBot initialization with custom native FastString buffer
 * - Multi-turn conversation context building
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
public class Benchmark {

    private ConversationHistory history;
    private MemoryContextBuilder contextBuilder;

    @Setup(Level.Trial)
    public void setup() {
        history = new ConversationHistory();
        history.system("You are a helpful AI assistant.");
        history.user("Hello, can you help me write a fast Java application?");
        history.assistant("Of course! FastJava provides zero-allocation primitives for high performance.");
        history.user("Tell me more about FastAIBot.");

        contextBuilder = new MemoryContextBuilder(new PlainTextFormatter());
    }

    @Benchmark
    public String benchmarkContextBuilderThroughput() {
        return contextBuilder.build(history);
    }

    @Benchmark
    public ConversationHistory benchmarkConversationTurnAppend() {
        ConversationHistory h = new ConversationHistory();
        h.system("System instruction");
        h.user("User input");
        h.assistant("Assistant response");
        return h;
    }
}
