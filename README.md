# 📊 ArrayBlockingQueue vs LinkedBlockingQueue 성능 비교분석

## 1. 개요 (Overview)

`BlockingQueue` 대표적 구현체인 `ArrayBlockingQueue`와 `LinkedBlockingQueue`의 성능 특성을 비교 분석한다.

### 가설

`LinkedBlockingQueue`는 생산 락(putLock)과 소비 락(takeLock)이 분리되어 있어, 단일 락을 사용하는 `ArrayBlockingQueue`보다 높은 동시성 성능을 보일 것이다.

### 목표

이론적인 락의 이점이 **실제 환경의 비용(cpu 캐시 미스)** 을 상쇄하고도 유의미한 성능 향상을 이끌어내는지 확인한다.

### 용어

* **생산자(Producer)**: 큐에 데이터를 넣는 주체 (`BlockingQueue.put()`)
* **소비자(Consumer)**: 큐에서 데이터를 꺼내는 주체 (`BlockingQueue.take()`)

<br>

## 2. 테스트 환경

* **하드웨어**: Apple MacBook Pro M4 (14 코어 CPU, 48GB RAM)
* **Producer 스레드:** 200개
* **Producer 당 발생 데이터 수:** 100,000(10만)건
* **총 데이터 수:** 20,000,000(2천만)건
* **변인 통제:** `ArrayBlockingQueue`의 큐 용량을 20,000,000(2천만)으로 설정하여, 큐가 차서 발생하는 Blocking 대기 시간을 배제하고 순수 자료구조의 처리 성능을 측정.

<br>

## 3. 테스트 결과

### 3-1. 단일 소비자

> **상황:** Producer(200) vs Consumer(1) - 락 경합이 비교적 적은 상황

| 큐 종류                   | 평균 소요 시간    | 처리율 (Throughput)     | 결과                   |
|:-----------------------|:------------|:---------------------|:---------------------|
| **ArrayBlockingQueue** | **875.4ms** | **약 22,846,755 건/초** | **✅ 최적 (약 1.5배 빠름)** |
| LinkedBlockingQueue    | 1,315.4ms   | 약 15,204,500 건/초     |                      |

### 3-2. 다중 소비자

> **상황:** Producer(200) vs Consumer(200) - 락 경합이 심한 상황

| 큐 종류                    | 평균 소요 시간      | 처리율 (Throughput)     | 결과                   |
|:------------------------|:--------------|:---------------------|:---------------------|
| ArrayBlockingQueue      | 1,287.2ms     | 약 15,537,508 건/초     |                      |
| **LinkedBlockingQueue** | **1,020.0ms** | **약 19,603,999 건/초** | **✅ 최적 (약 1.3배 빠름)** |

<details>
<summary>📋 측정 데이터</summary>

> 각 경우에 대해 5회씩 측정한 개별 시간과 평균 시간.

**1. Single Consumer - ArrayBlockingQueue**

- Time: 897ms, 834ms, 846ms, 918ms, 882ms
- Avg: 875.4ms

**2. Single Consumer - LinkedBlockingQueue**

- Time: 1266ms, 1362ms, 1373ms, 1419ms, 1157ms
- Avg: 1315.4ms

**3. Multi Consumer - ArrayBlockingQueue**

- Time: 1270ms, 1307ms, 1318ms, 1268ms, 1273ms
- Avg: 1287.2ms

**4. Multi Consumer - LinkedBlockingQueue**

- Time: 1073ms, 1081ms, 967ms, 951ms, 1029ms
- Avg: 1020.0ms

</details>

<br>

## 4. 분석

### 💡 핵심 발견

> "`LinkedBlockingQueue`는 생산 락(putLock)과 소비 락(takeLock)이 분리되어 있어, 단일 락을 사용하는 `ArrayBlockingQueue`보다 높은 동시성 성능을 보일 것" 이라는
> 가설은 **거짓**이다.
>
> 성능은 **[락 경합 비용]** vs **[cpu 캐시 미스 비용]** 의 관점에서 결정된다.

### 4-1. 왜 단일 소비자 상황에서는 `ArrayBlockingQueue`가 더 빠른가?

생산자 스레드가 많더라도 소비자 스레드가 하나뿐인 상황에서는 소비/생산 락 분리의 이점이 크지 않다.

오히려 `LinkedBlockingQueue`가 가진 구조적 비용이 성능 저하의 주원인이 된다.

1. **CPU 캐시 미스:** 연결 리스트 구조는 노드가 메모리에 흩어져 있어 캐시의 공간적 지역성이 떨어진다. <br> 반면, 배열 기반인 `ArrayBlockingQueue`는 캐시
   히트율이 높아 처리가 매우 빠르다.
2. **의미없는 락 분리:** 소비자가 하나뿐이므로, 삽입과 추출이 동시에 일어날 일이 거의 없다. <br> 따라서 락 분리의 이점이 거의 없다.

### 4-2. 왜 다중 소비자 상황에서는 `LinkedBlockingQueue`가 더 빠른가?

생산자와 소비자가 모두 많아지면(400개 스레드), `ArrayBlockingQueue`의 단일 락으로 인한 경합 비용이 메모리 비용보다 훨씬 더 큰 문제가 된다.

1. **단일 락의 한계:** `ArrayBlockingQueue`는 삽입/소비 작업 시, 하나의 락을 공유한다. <br> 수백 개의 스레드가 하나의 락을 두고 경쟁하기 때문에, 대기 시간이 길어진다.
2. **소비 락과 생산 락 분리의 장점:** `LinkedBlockingQueue`는 생산 락(putLock)과 소비 락(takeLock)이 분리되어 있어, 삽입과 추출이 동시에 일어날 수 있다. <br> 극심한
   경합 상황에서는 **캐시 미스 비용** 을 지불하더라도, 동시성 처리량을 늘리는 것이 전체 성능에 더 유리하다.

<br>

## 5. 결론

`BlockingQueue`의 소비/삽입 패턴에 따라 적절한 구현체를 선택해야 한다.

### `ArrayBlockingQueue`가 적절한 경우

Queue 크기가 고정되어도 문제없는 경우.
스레드 경합이 심하지 않은 경우(생산자, 소비자 비율이 불균형한 경우)

### `LinkedBlockingQueue`가 적절한 경우

Queue 크기가 가변적이거나, 예측하기 어려운 경우.
생산자와 소비자가 모두 많아 동시 접근(경합)이 매우 심한 경우.
스레드 경합이 매우 심한 경우(생산자, 소비자가 비율이 비슷한 경우)

