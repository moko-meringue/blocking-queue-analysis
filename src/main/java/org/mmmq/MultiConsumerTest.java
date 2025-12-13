package org.mmmq;

import java.util.concurrent.*;

class MultiConsumerTest {

    long test(BlockingQueue<Message> queue, int producerSize, int messagePerProducer, int consumerSize) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(producerSize * messagePerProducer);
        ExecutorService producerPool = Executors.newFixedThreadPool(producerSize);
        ExecutorService consumerPool = Executors.newFixedThreadPool(consumerSize);

        for (int i = 0; i < producerSize; i++) {
            producerPool.submit(() -> {
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

        for (int i = 0; i < consumerSize; i++) {
            consumerPool.submit(() -> {
                try {
                    startLatch.await();
                    while (true) {
                        if (endLatch.getCount() == 0) {
                            break;
                        }
                        if (queue.poll(2, TimeUnit.SECONDS) == null) {
                            if (endLatch.getCount() == 0) {
                                break;
                            }
                            continue;
                        }
                        endLatch.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis();
        producerPool.shutdownNow();
        consumerPool.shutdownNow();
        return endTime - startTime;
    }
}
