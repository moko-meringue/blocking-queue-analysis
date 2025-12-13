package org.mmmq;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SingleConsumerTest {

    long test(BlockingQueue<Message> queue, int producerSize, int messagePerProducer) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(producerSize * messagePerProducer);
        ExecutorService executor = Executors.newFixedThreadPool(producerSize);

        for (int i = 0; i < producerSize; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < messagePerProducer; j++) {
                        queue.put(
                                new Message(
                                        "topic" + Thread.currentThread().getId(),
                                        "data" + Thread.currentThread().getId()
                                )
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        while (endLatch.getCount() > 0) {
            try {
                queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endLatch.countDown();
            }
        }
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        return endTime - startTime;
    }
}
