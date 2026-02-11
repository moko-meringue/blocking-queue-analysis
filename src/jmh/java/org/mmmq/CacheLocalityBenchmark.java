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

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class CacheLocalityBenchmark {

    @Param({"ArrayBlockingQueue", "LinkedBlockingQueue"})
    public String queueType;

    @Param({"1000000"})
    public int size;

    private BlockingQueue<Message> queue;
    private ArrayList<Message> drainTarget;

    @Setup(Level.Invocation)
    public void setup() throws InterruptedException {
        if ("ArrayBlockingQueue".equals(queueType)) {
            queue = new ArrayBlockingQueue<>(size);
        } else {
            queue = new LinkedBlockingQueue<>(size);
        }
        drainTarget = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            queue.put(new Message("topic", "data"));
        }
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        queue.clear();
        drainTarget.clear();
    }

    @Benchmark
    public int drainTest() {
        return queue.drainTo(drainTarget);
    }

    @Benchmark
    public int iterateTest() {
        int sum = 0;
        for (Message m : queue) {
            sum += m.hashCode();
        }
        return sum;
    }
}
