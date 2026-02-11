package org.mmmq;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class DataStructureThroughputBenchmark {

    @Param({"ArrayBlockingQueue", "LinkedBlockingQueue"})
    public String queueType;

    @Param({"1000000"})
    public int capacity;

    private BlockingQueue<Message> queue;
    private Message message;

    @Setup(Level.Trial)
    public void setup() {
        if ("ArrayBlockingQueue".equals(queueType)) {
            queue = new ArrayBlockingQueue<>(capacity);
        } else {
            queue = new LinkedBlockingQueue<>(capacity);
        }
        message = new Message("topic", "data");
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        queue.clear();
    }

    @Benchmark
    public void offerPoll() {
        queue.offer(message);
        queue.poll();
    }
}
