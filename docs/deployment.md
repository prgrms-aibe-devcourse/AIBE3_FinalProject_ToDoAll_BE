# 운영 배포 (무중단 Blue/Green) 아키텍처 

본 문서는 Jobda 백엔드 서버의 실제 운영 환경 기준 배포 구조를 설명합니다.

---

## 1. 배포 전략 

- Docker Compose 기반 Blue/Green 무중단 배포
- 서비스 중단 없이 신규 버전 배포 및 롤백 가능하도록 설정 
- Nginx를 통해 트래픽을 단일 포트로 라우팅

---

## 2. Blue / Green 구조

| 구분 | 컨테이너 | 내부 포트 |
|----|--------|----------|
| Blue | jobda-app-blue | 8080 |
| Green | jobda-app-green | 8081 |

- 두 컨테이너는 동시에 실행되며, 한쪽만 실제 트래픽을 수신하도록 구현
- 현재 활성화된 컨테이너는 `CURRENT_COLOR` 파일로 관리함

---

## 3. 배포 흐름

1. GitHub Actions에서 Docker Image 빌드 및 Registry Push
2. EC2 서버에 SSH 접속 후 `deploy.sh` 실행
3. 현재 활성 컨테이너와 반대 색상의 컨테이너를 배포 대상으로 선택
4. 신규 컨테이너 실행 
5. `/actuator/health/liveness` 엔드포인트를 통해 헬스체크 수행
6. 헬스체크 성공 시 Nginx upstream을 신규 컨테이너 포트로 전환
7. Nginx reload 후 트래픽 무중단 전환 완료

---

## 4. 헬스체크 기준

- Spring Boot Actuator의 `liveness` 엔드포인트 사용
- DB, Redis 등 외부 의존성은 제외하여 배포 안정성 확보
- 헬스체크 실패 시 배포 중단 및 기존 서비스 유지

---

## 5. Nginx 구성 방식

- 실제 클라이언트 요청은 단일 도메인(`be.jobda.store`)으로 유입
- `jobda-upstream.conf` 파일을 배포 시점에 동적으로 갱신
- 업스트림 변경 후 `nginx reload`로 트래픽 전환

---

## 6. 롤백 전략

- 신규 배포 실패 시 upstream 전환을 수행하지 않음
- 기존 컨테이너는 종료하지 않고 즉시 복구 가능
- 필요 시 이전 색상 컨테이너로 수동 전환 가능함 

---

## 7. 배포 스크립트 위치

- `infra/deploy/deploy.sh`
- `infra/nginx/be.jobda.store.conf`
- `infra/nginx/jobda-upstream.conf.template`