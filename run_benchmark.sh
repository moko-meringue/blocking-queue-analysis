#!/bin/bash

./gradlew clean jmhJar

sudo sysctl -w kernel.perf_event_paranoid=-1
sudo sysctl -w kernel.kptr_restrict=0

CORES=(2 4)
CAPACITY_PERCENTS=(10 100)

BASE_DIR="benchmark_result"
mkdir -p $BASE_DIR

MAX_CORE=$(($(nproc) - 1))

for i in "${!CORES[@]}"; do
  CORE=${CORES[$i]}

  # 격리 코어(벤치마크용)와 비격리 코어(OS용) 분할
  if [ "$CORE" -eq 2 ]; then
    SHIELD_CORES="0-1"
    OS_CORES="2-${MAX_CORE}"
  elif [ "$CORE" -eq 4 ]; then
    SHIELD_CORES="0-3"
    OS_CORES="4-${MAX_CORE}"
  fi

  for PERCENT in "${CAPACITY_PERCENTS[@]}"; do

    DIR_NAME="C${CORE}-CAPA${PERCENT}"
    RESULT_DIR="${BASE_DIR}/${DIR_NAME}"
    JSON_NAME="${DIR_NAME}.json"

    mkdir -p "${RESULT_DIR}"

    echo "======================================================"
    echo "Running config: CPU=${CORE} Core, QueueCapacity=${PERCENT}%"
    echo "Target Directory: ${RESULT_DIR}"
    echo "======================================================"

    # OS의 모든 시스템/유저 프로세스를 OS_CORES로 강제 이주
    echo "Moving OS processes to cores ${OS_CORES} to isolate cores ${SHIELD_CORES}..."
    sudo systemctl set-property system.slice AllowedCPUs=${OS_CORES}
    sudo systemctl set-property user.slice AllowedCPUs=${OS_CORES}
    sudo systemctl set-property init.scope AllowedCPUs=${OS_CORES}

    # 격리된 SHIELD_CORES에만 자바 프로세스 할당
    echo "Executing JMH benchmark on isolated cores ${SHIELD_CORES}..."
        sudo systemd-run --scope -p AllowedCPUs=${SHIELD_CORES} -- \
          java -XX:ActiveProcessorCount=${CORE} \
          -jar build/libs/*-jmh.jar \
          -p capacityPercent=${PERCENT} \
          -gc true \
          -prof gc \
          -prof "perfnorm:events=instructions,cycles,L1-dcache-loads,L1-dcache-load-misses,LLC-loads,LLC-load-misses,mem_load_l3_hit_retired.xsnp_hitm,mem_load_l3_hit_retired.xsnp_hit" \
          -prof jfr:dir=${RESULT_DIR} \
          -prof stack \
          -rf json -rff ${RESULT_DIR}/${JSON_NAME}

    # 시스템 프로세스의 코어 제한을 원래대로(전체 코어) 복구
    sudo systemctl revert system.slice user.slice init.scope

  done
done
