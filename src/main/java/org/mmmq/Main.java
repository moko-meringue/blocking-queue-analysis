package org.mmmq;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    /**
     * 캐시 히트, gc등으로 인한 변인을 통제하기 위해 각 테스트는 주석을 해제하고 개별적으로 실행해야 한다.
     */
    public static void main(final String[] args) {
//        단일_Consumer_환경에서_ArrayBlockingQueue_성능을_측정한다();
//        단일_Consumer_환경에서_LinkedBlockingQueue_성능을_측정한다();
//
//        다중_Consumer_환경에서_ArrayBlockingQueue_성능을_측정한다();
//        다중_Consumer_환경에서_LinkedBlockingQueue_성능을_측정한다();
    }

    /**
     * <h2>단일 Consumer 환경에서 ArrayBlockingQueue 성능을 측정한다</h2>
     *
     * <h3>환경</h3>
     * <p>맥북 m4 pro 14 Core CPU, 48GB RAM</p>
     * <p>생산자 200, 소비자 1</p>
     * <p>생산자는 100,000(10만)개의 메시지를 발행한다.</p>
     * <p>총 메시지 수: 20,000,000(2천만)개</p>
     * <p>ArrayBlockingQueue의 크기로 인해 Blocking되는 변인을 통제하기 위해 큐의 크기를 20,000,000(2천만)으로 설정한다.</p>
     *
     * <h3>결과</h3>
     * <p>소요 시간: <strong>897 ms, 834 ms, 846 ms, 918 ms, 882 ms</strong></p>
     * <p>평균 소요 시간: <strong>875.4 ms</strong></p>
     * <p>처리율: <strong>약 22,846,755 건/초</strong></p>
     */
    static void 단일_Consumer_환경에서_ArrayBlockingQueue_성능을_측정한다() {
        SingleConsumerTest singleConsumerTest = new SingleConsumerTest();
        ArrayBlockingQueue<Message> arrayBlockingQueue = new ArrayBlockingQueue<>(20_000_000);
        long result = singleConsumerTest.test(arrayBlockingQueue, 200, 100_000);
        System.out.println("단일 Consumer ArrayBlockingQueue 처리 시간: " + result + " ms");
    }

    /**
     * <h2>단일 Consumer 환경에서 LinkedBlockingQueue 성능을 측정한다</h2>
     *
     * <h3>환경</h3>
     * <p>맥북 m4 pro 14 Core CPU, 48GB RAM</p>
     * <p>생산자 200, 소비자 1</p>
     * <p>생산자는 100,000(10만)개의 메시지를 발행한다.</p>
     * <p>총 메시지 수: 20,000,000(2천만)개</p>
     *
     * <h3>결과</h3>
     * <p>소요 시간: <strong>1,266 ms, 1,362 ms, 1,373 ms, 1,419 ms, 1,157 ms</strong></p>
     * <p>평균 소요 시간: <strong>1,315.4 ms</strong></p>
     * <p>처리율: <strong>약 15,204,500 건/초</strong></p>
     */
    static void 단일_Consumer_환경에서_LinkedBlockingQueue_성능을_측정한다() {
        SingleConsumerTest singleConsumerTest = new SingleConsumerTest();
        LinkedBlockingQueue<Message> linkedBlockingQueue = new LinkedBlockingQueue<>();
        long result = singleConsumerTest.test(linkedBlockingQueue, 200, 100_000);
        System.out.println("단일 Consumer LinkedBlockingQueue 처리 시간: " + result + " ms");
    }

    /**
     * <h2>다중 Consumer 환경에서 ArrayBlockingQueue 성능을 측정한다</h2>
     *
     * <h3>환경</h3>
     * <p>맥북 m4 pro 14 Core CPU, 48GB RAM</p>
     * <p>생산자 200, 소비자 200</p>
     * <p>생산자는 100,000(10만)개의 메시지를 발행한다.</p>
     * <p>총 메시지 수: 20,000,000(2천만)개</p>
     * <p>ArrayBlockingQueue의 크기로 인해 Blocking되는 변인을 통제하기 위해 큐의 크기를 20,000,000(2천만)으로 설정한다.</p>
     *
     * <h3>결과</h3>
     * <p>소요 시간: <strong>1,270 ms, 1,307 ms, 1,318 ms, 1,268 ms, 1,273 ms</strong></p>
     * <p>평균 소요 시간: <strong>1,287.2 ms</strong></p>
     * <p>처리율: <strong>약 15,537,508.9 건/초</strong></p>
     */
    static void 다중_Consumer_환경에서_ArrayBlockingQueue_성능을_측정한다() {
        MultiConsumerTest multiConsumerTest = new MultiConsumerTest();
        ArrayBlockingQueue<Message> arrayBlockingQueue = new ArrayBlockingQueue<>(20_000_000);
        long result = multiConsumerTest.test(arrayBlockingQueue, 200, 100_000, 200);
        System.out.println("다중 Consumer ArrayBlockingQueue 처리 시간: " + result + " ms");
    }

    /**
     * <h2>다중 Consumer 환경에서 LinkedBlockingQueue 성능을 측정한다</h2>
     *
     * <h3>환경</h3>
     * <p>맥북 m4 pro 14 Core CPU, 48GB RAM</p>
     * <p>생산자 200, 소비자 1</p>
     * <p>생산자는 100,000(10만)개의 메시지를 발행한다.</p>
     * <p>총 메시지 수: 20,000,000(2천만)개</p>
     *
     * <h3>결과</h3>
     * <p>소요 시간: <strong>1073 ms, 1081 ms, 967 ms, 951 ms, 1029 ms</strong></p>
     * <p>평균 소요 시간: <strong>1020 ms</strong></p>
     * <p>처리율: <strong>약 19,603,999.2 건/초</strong></p>
     */
    static void 다중_Consumer_환경에서_LinkedBlockingQueue_성능을_측정한다() {
        MultiConsumerTest multiConsumerTest = new MultiConsumerTest();
        LinkedBlockingQueue<Message> linkedBlockingQueue = new LinkedBlockingQueue<>();
        long result = multiConsumerTest.test(linkedBlockingQueue, 200, 100_000, 200);
        System.out.println("다중 Consumer LinkedBlockingQueue 처리 시간: " + result + " ms");
    }
}
