package org.mmmq;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@State(Scope.Group)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class BlockingQueueBenchmark {

    @Param({"ArrayBlockingQueue", "LinkedBlockingQueue"})
    public String queueType;

    @Param({"20000000"})
    public int capacity;

    private BlockingQueue<Message> queue;

    @Setup(Level.Trial)
    public void setup() {
        if ("ArrayBlockingQueue".equals(queueType)) {
            queue = new ArrayBlockingQueue<>(capacity);
        } else {
            queue = new LinkedBlockingQueue<>();
        }
    }

    @Benchmark
    @Group("singleConsumer")
    @GroupThreads(1)
    public void singleConsumerTake(Blackhole bh) throws InterruptedException {
        bh.consume(queue.take());
    }

    @Benchmark
    @Group("singleConsumer")
    @GroupThreads(200)
    public void singleConsumerPut() throws InterruptedException {
        queue.put(new Message("topic", "data"));
    }

    @Benchmark
    @Group("multiConsumer")
    @GroupThreads(200)
    public void multiConsumerTake(Blackhole bh) throws InterruptedException {
        bh.consume(queue.take());
    }

    @Benchmark
    @Group("multiConsumer")
    @GroupThreads(200)
    public void multiConsumerPut() throws InterruptedException {
        queue.put(new Message("topic", "data"));
    }
}
