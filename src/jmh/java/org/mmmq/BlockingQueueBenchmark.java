package org.mmmq;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.Control;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class BlockingQueueBenchmark {

    private static final int BASE_QUEUE_CAPACITY = 20_000_000;
    private static final int THREAD_COUNT = 100;

    // ==========================================
    // 1. ABQ - MPSC
    // ==========================================
    @Benchmark
    @Group("ABQ_1")
    @GroupThreads(THREAD_COUNT)
    public void abq1Producer(QueueState state, Control control) {
        while (!control.stopMeasurement) {
            if (state.abq.offer(state.dummyMessage)) {
                break;
            }
        }
    }

    @Benchmark
    @Group("ABQ_1")
    @GroupThreads(1)
    public void abq1Consumer(QueueState state, Control control, Blackhole bh) {
        while (!control.stopMeasurement) {
            Message msg = state.abq.poll();
            if (msg != null) {
                bh.consume(msg);
                break;
            }
        }
    }

    // ==========================================
    // 2. ABQ - MPMC
    // ==========================================
    @Benchmark
    @Group("ABQ_" + THREAD_COUNT)
    @GroupThreads(THREAD_COUNT)
    public void abq100Producer(QueueState state, Control control) {
        while (!control.stopMeasurement) {
            if (state.abq.offer(state.dummyMessage)) {
                break;
            }
        }
    }

    @Benchmark
    @Group("ABQ_" + THREAD_COUNT)
    @GroupThreads(THREAD_COUNT)
    public void abq100Consumer(QueueState state, Control control, Blackhole bh) {
        while (!control.stopMeasurement) {
            Message msg = state.abq.poll();
            if (msg != null) {
                bh.consume(msg);
                break;
            }
        }
    }

    // ==========================================
    // 3. LBQ - MPSC
    // ==========================================
    @Benchmark
    @Group("LBQ_1")
    @GroupThreads(THREAD_COUNT)
    public void lbq1Producer(QueueState state, Control control) {
        while (!control.stopMeasurement) {
            if (state.lbq.offer(state.dummyMessage)) {
                break;
            }
        }
    }

    @Benchmark
    @Group("LBQ_1")
    @GroupThreads(1)
    public void lbq1Consumer(QueueState state, Control control, Blackhole bh) {
        while (!control.stopMeasurement) {
            Message msg = state.lbq.poll();
            if (msg != null) {
                bh.consume(msg);
                break;
            }
        }
    }

    // ==========================================
    // 4. LBQ - MPMC
    // ==========================================
    @Benchmark
    @Group("LBQ_" + THREAD_COUNT)
    @GroupThreads(THREAD_COUNT)
    public void lbq100Producer(QueueState state, Control control) {
        while (!control.stopMeasurement) {
            if (state.lbq.offer(state.dummyMessage)) {
                break;
            }
        }
    }

    @Benchmark
    @Group("LBQ_" + THREAD_COUNT)
    @GroupThreads(THREAD_COUNT)
    public void lbq100Consumer(QueueState state, Control control, Blackhole bh) {
        while (!control.stopMeasurement) {
            Message msg = state.lbq.poll();
            if (msg != null) {
                bh.consume(msg);
                break;
            }
        }
    }

    @State(Scope.Group)
    public static class QueueState {

        @Param({"10", "100"})
        public int capacityPercent;
        public ArrayBlockingQueue<Message> abq;
        public LinkedBlockingQueue<Message> lbq;
        public Message dummyMessage;

        @Setup(Level.Iteration)
        public void setUp() {
            int actualCapacity = (int) (BASE_QUEUE_CAPACITY * (capacityPercent / 100.0));
            abq = new ArrayBlockingQueue<>(actualCapacity);
            lbq = new LinkedBlockingQueue<>(actualCapacity);
            dummyMessage = new Message("topic", "data");
            IterationStartMarker event = new IterationStartMarker();
            event.message = "Start";
            event.commit();
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            abq.clear();
            lbq.clear();
        }
    }
}
