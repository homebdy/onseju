# 온세주: 온 세상이 다 주식이야!

### 📄 프로젝트 개요
- 주제: 국내 주식 기반 모의 투자 서비스
- 대상: 주식을 처음 시작하는 예비 투자자

### 📽️ 시연 영상
[시연 영상 보러 가기](https://drive.google.com/file/d/1KyZ7CrdTOSZ2oj19dpbv4yErDoSfwY2L/view?usp=sharing)

### 🎯 목표
- 맞춤 주식 추천: 사용자가 보고 있는 내용에 대한 주식 추천
- 모의 투자 서비스 제공: 관심있는 주식에 대한 모의 투자기능 제공

### 📚 기술 스택
![스크린샷 2025-05-08 오후 5 01 11](https://github.com/user-attachments/assets/df40d128-5c59-44a0-81d2-24af99d6784d)


### ✏️ 아키텍쳐
![제목 없는 다이어그램 drawio](https://github.com/user-attachments/assets/20e75d54-e6c1-4369-9f82-523f67c429bf)

> 위 그림은 온세주의 전체 서비스 아키텍처를 나타냅니다.  
> - **User Service**: 인증, 잔액 및 보유 주식 관리  
> - **Order Service**: 주문 생성 및 저장  
> - **Matching Service**: 주문 이벤트 기반 체결 진행  


### 💬 주요 역할
- 주문 및 체결 알고리즘 구현
- 체결 및 주문 시 이벤트 처리

### 🔗 주요 기능

1. **회원가입 및 인증 시스템**
    - Google 소셜 로그인 지원
    - 마이페이지에서 사용자 정보 및 관심 주식 관리

2. **크롬 익스텐션 기반 주식 추천**
    - 사용자가 보고 있는 웹 페이지 기반 관련 주식 자동 추천
    - 관심 주식 저장 기능 제공

3. **주식 주문 및 체결 시스템**
    - 사용자가 주문한 주식을 시장 규칙에 따라 체결
    - 비동기 이벤트 기반 처리 구조로 구현


### 🌏 주요 로직 플로우 차트
<img width="943" alt="주문 처리 흐름도" src="https://github.com/user-attachments/assets/0edb90be-ebb0-4b72-a83b-9baf6ed56ba8" />

### 💪 성능 개선

1️⃣ **체결 속도 향상** - TPS 약 625% 향상

**문제 상황**
- DB를 조회하며 체결을 진행할 경우 Connection Pool 부족과 속도 저하 문제 발견

**해결 방법**
1. 인메모리 상에서 체결 진행
    - 기존 DB에서 조회 -> 남은 수량 업데이트 -> 저장 로직을 인메모리 상 TreeMap에서 진행
    - 데이터 유실 방지를 위해 체결 완료 후 DB에 데이터 저장
2. 동시성 문제 대응
   - 자료구조를 ConcurrentSkipMap으로 변경 및 ReentrantLock 적용
   - DB 저장 시 발생하는 동시성 문제는 낙관적 락 + 지수 백오프 방식 적용

**결과**

1. DB 기반 체결 처리(약 19TPS)
<img width="1243" alt="image (3)" src="https://github.com/user-attachments/assets/bcdb1daa-1a32-44d9-be96-963fabfa1bd3" />

2. 인메모리 기반 체결(약 160TPS)
<img width="1218" alt="image (4)" src="https://github.com/user-attachments/assets/1bc9c8f3-6a72-4546-b26b-11b8c6fe34df" />

<br/>

2️⃣ **체결 엔진 메모리 부족 문제 해결**

**문제 상황**
- 20개 회사 대상 부하테스트 결과 약 450MB 힙 사용 확인
- 900개 회사로 확장할 경우 20GB 메모리가 필요할 것으로 예상

**해결**
- 서버를 사용자/주문/체결 기능으로 분리
- 각 서비스 간 데이터 처리를 RabbitMQ 기반의 이벤트 구조로 구성
