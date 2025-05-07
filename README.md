# Study Matching System

## 🛠️ 프로젝트 개요

본 프로젝트는 사용자가 스터디를 쉽고 빠르게 찾을 수 있도록 도와주는 매칭 플랫폼입니다. 사용자와 스터디 간의 연결을 매끄럽게 만들어 주며, 회원가입부터 스터디 생성, 신청 및 수락까지의 전체 프로세스를 다룹니다. 이 시스템은 콘솔 기반 Java 애플리케이션으로 개발되었으며, Spring 등의 프레임워크를 사용하지 않고 순수 Java로 구현되었습니다.

## ⚙️ 기술 스택
- Java (순수 Java 기반, Spring 미사용)
- Oracle DB
- JDBC
- Git

## 🧭 주요 기능

### 1. 사용자 관리
- 회원가입: 사용자 정보 입력 후 계정 생성
- 로그인: 등록된 사용자 계정으로 로그인
- 정보 수정: 사용자 프로파일 정보 수정
- 계정 삭제: 계정 및 관련 정보 삭제

### 2. 스터디 관리
- 스터디 생성: 새로운 스터디 개설 및 정보 입력
- 스터디 신청: 원하는 스터디에 가입 신청
- 신청 수락/거절: 스터디장은 신청된 사용자들을 관리할 수 있음

### 3. 알림 및 추천
- 알림: 신청 및 수락 여부에 대한 알림 제공
- 추천: 거리 기반 및 태그 유사도를 활용한 스터디 추천

## 🗂️ CRUD 작업 요약
- **Create:** 회원가입 및 스터디 생성
- **Read:** 사용자 정보 조회 및 스터디 목록 확인
- **Update:** 사용자 정보 수정
- **Delete:** 계정 삭제 및 스터디 삭제

## 📦 데이터베이스 구조
- **User 테이블**: 사용자 이름, 나이, 위치, 관심 태그 등
- **Study 테이블**: 스터디 이름, 설명, 태그, 모임 방식 (온라인/오프라인), 위치 정보 등
- **Application 테이블**: 사용자 신청 내역 및 상태 정보

## ✅ 설치 및 실행 방법

### 환경 설정 방법:

1. `src/db.properties` 파일 생성:
   - `src` 폴더 내에 `db.properties` 파일을 생성하고 아래 내용을 추가합니다:

   ```properties
   db.url=jdbc:oracle:thin:@[YOUR_IP_ADDRESS]:1521:xe
   db.user=[YOUR_USERNAME]
   db.password=[YOUR_PASSWORD]
   ```

2. `.gitignore`에 `db.properties` 추가:

   ```plaintext
   src/db.properties
   ```

3. 환경 변수를 사용하려면 아래와 같이 설정합니다:

   **Windows:**
   ```bash
   set DB_URL=jdbc:oracle:thin:@[YOUR_IP_ADDRESS]:1521:xe
   set DB_USER=[YOUR_USERNAME]
   set DB_PASSWORD=[YOUR_PASSWORD]
   ```

   **Unix/Linux/Mac:**
   ```bash
   export DB_URL=jdbc:oracle:thin:@[YOUR_IP_ADDRESS]:1521:xe
   export DB_USER=[YOUR_USERNAME]
   export DB_PASSWORD=[YOUR_PASSWORD]
   ```
1. 저장소 클론:
```bash
git clone https://github.com/PanicAthe/StudyMatching.git
```

2. 데이터베이스 설정:
- Oracle DB에 테이블을 생성하고 필요한 데이터베이스 연결 설정을 구성합니다.

3. 애플리케이션 실행:
- `Main.java` 파일을 실행하여 애플리케이션을 시작합니다.

## 📌 향후 확장성
- Spring Framework를 통한 웹 애플리케이션 전환
- 실시간 알림 시스템 구축
- 다양한 필터링 및 검색 기능 추가


---

이 프로젝트는 순수 Java와 Oracle DB를 활용하여 효율적인 매칭 플랫폼을 구현하는 데 중점을 두고 있습니다. 사용자는 쉽고 빠르게 스터디를 찾고 신청할 수 있으며, 스터디장은 신청된 사용자들을 효율적으로 관리할 수 있습니다.
