[![인턴하샤](https://private-user-images.githubusercontent.com/127807229/408776202-b59ba8a2-5b70-49f7-a39e-4c273f3a6012.svg?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3MzgzODY1NzgsIm5iZiI6MTczODM4NjI3OCwicGF0aCI6Ii8xMjc4MDcyMjkvNDA4Nzc2MjAyLWI1OWJhOGEyLTViNzAtNDlmNy1hMzllLTRjMjczZjNhNjAxMi5zdmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjUwMjAxJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI1MDIwMVQwNTA0MzhaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT1lYjdjZWI2N2ZiNzJiOWFlOTEwZDRiZjFkNzQ4ZjU0OGJkY2Q0NDg3OWYxNDQ0ZjI1ZGNlNThjOGRiOWY2YzE3JlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCJ9.UOZCSBAat6up7ERaSrvv2_N1GgN-G_lIVx8uFj4rP5M)](https://www.survey-josha.site/)

# 🎥 인턴하샤 🎥 - 와플스튜디오 21.5기 1조 (웹 서버)

- 🧇[**인턴하샤**](https://www.survey-josha.site/) 는 1조의 기획 프로젝트입니다!
- 인턴하샤는 스타트업과, 스타트업 인턴십을 구하는 학생들을 매칭시켜주는 역할을 합니다.
- 많이 부족하지만 열과 성을 다해 제작한 저희 인턴하샤 서비스를 만족스럽게 사용하실 수 있기를 바랍니다🙏🙏
- FRONT 개발에 관해 궁금하시다면? [TEAM1-FRONT REPO](https://github.com/wafflestudio/22-5-team1-web)
  <br/><br/>

## 📈 기획

### 니즈 확인

- https://eng.snu.ac.kr/snu/bbs/BMSR00004/view.do?boardId=1875&menuNo=200176
- https://slashpage.com/match-snucba?lang=ko
- SNAAC에서도 자체적으로 진행 결과 있음

니즈가 존재하는 부분. 이에 따라 우리가 기능을 확실하게 구현해 그 니즈에서의 주된 플레이어가 되는 것을 목표.

### 페인 포인트

- 스타트업: 우리가 좋은 초기 스타트업인데, 직원 구하기가 어렵다
    - 분산된 창구(경력개발원, 단톡방, 에타)
    - 인맥과 수고가 필요
- 구직자: 인턴하고 싶은데 좋은 스타트업 찾기가 어렵다
    - 일단 기본적으로 좋은 스타트업이 많지 않음
    - 스타트업 구직 창구가 분산되어 있어 비교도 쉽지 않음
    - 좋은 정보에 대한 접근도 많은 수고가 들며, 내부 정보(IR 자료 등) 등은 아예 확인 불가

### 기획 의도

- 분산된 창구를 모은다
    - 스타트업의 수고를 줄이며, 구직자에게도 정보를 한눈에 모아볼 수 있게
- 좋은 스타트업을 고른다
    - SNAAC, SNUSV, VC, 교내 경력개발원 등 단체와의 협업을 통해, 그들이 한정된 수의 추천 티켓을 가지고 믿을만한 스타트업을 추천하도록 한다.
- 좋은 정보를 제공한다
    - IR 자료는 투자심의 때의 자료로, 스타트업의 방향성, 내부 정보등 가치 있는 정보들을 담고 있음
    - 온라인에 그냥 올려두면 최신이 아닌 자료가 올라가 있을 수 있다는 문제 때문에, 공개적이지 않음
    - 폐쇄형 커뮤니티에는 걱정 없이 업로드 가능할 것 같다는 것이 현재 SNAAC과의 컨택에서 얻은 답변
    - 추가적인 정보에 대해서는 논의 필요.
- 최대한 구직자 친화적으로 제공한다
    - 어차피 구직자의 수가 많고, 질 높은 사람들을 모을 수 있다면 좋은 스타트업들은 알아서 들어온다
    - 구직 기능을 제외하더라도 학생 트래픽을 최대한 높일 수 있는 기능이라면 도입이 필요하다

### 확장 가능성

- 커뮤니티 / 아티클  기능 추가
    - 아티클 기능의 예시
        - [https://eopla.net](https://eopla.net/)
        - https://www.tokyodev.com/articles
        - 아티클 기능에서는 eo플래닛이 강점이 있어, 차별화 포인트를 모색해보아야 할듯
            - 대표의 모교를 다니는 학생에게만 글이 보인다든가?
    - 학생 트래픽을 통한 선순환을 목표로 함
- SKP/KY/SSH까지 확대
    - 충분한 트래픽을 얻어내는 것과, 구직자풀 QC의 균형
        - 학벌을 통한 QC가 근원적인 해결책이라고는 생각하지 않음. 유저의 니즈 조사 통한 방안 확인해야 할 것.
    - 다만 학벌을 통한 일차적인 스크리닝을 통해, 희소성 효과를 만들어낼 수 있음
        - 희소성 효과: 아무나 얻는 기회가 아니니 좋을 것이다
- 디스코드 서버/단톡방 등 유저에게 노출될 창구 늘리기
    - 구직자를 모으는 것이 서비스의 성패를 결정함
- 이외 유저가 바라는 것이라면 무엇이든

## 💻 배포

- 프론트엔드 서버 도메인(Web server) : <https://www.survey-josha.site/>
- 백엔드 서버 도메인(Api server) : <https://api.survey-josha.site/>

## 👥 팀원
- **[최장혁](https://github.com/goranikin) - 팀장, 프론트엔드**
- **[김연우](https://github.com/Yeonu-Kim) - 프론트엔드**
- **빈채현 - 디자인**
- **[이종현](https://github.com/lukeqwaszx) - 백엔드**
- **[임광섭](https://github.com/endermaru) - 백엔드**


## 🙋‍♂️ 역할 분담

|                                                                                             |                                                                                    |
| :-----------------------------------------------------------------------------------------: | :--------------------------------------------------------------------------------: |
|                                           이종현                                            |                                       임광섭                                       |
|                     [@lukeqwaszx](https://github.com/lukeqwaszx)                    |                    [@endermaru](https://github.com/endermaru)                    |
|  User 패키지 리팩토링, Exception Handler, Post 패키지 CRUD 등 Curator 관련 서비스 구현 |  초기 세팅 및 각 패키지 별 기본 서비스 구현, Post 필터링, SMTP 서비스, Redis |
</div>


## 🚀 기술 스택(백엔드)

- **애플리케이션 개발**: Kotlin, Spring Boot 3.4.1, JPA
- **데이터베이스**: MySQL, H2 (테스트용)
- **인프라** : AWS EC2, S3, RDS
- **API 문서화**: SpringDoc OpenAPI 2.7.0
- **캐시 및 환경 관리**: Redis, dotenv-kotlin
- **빌드 및 관리**: Gradle, Dependency Management Plugin, Ktlint
- **배포** : Docker, Docker Compose

## 🛠️ 주요 기능

1. **회원가입**
  - 로컬 회원가입 : 로컬 로그인 아이디, 비밀번호 + 스누메일 인증
  - 구글 회원가입(스누메일) : 원터치 회원가입
  - 구글 회원가입(스누메일X) : 구글 OAuth 인증 + 스누메일 인증
  - 동일한 스누메일에 대해 로컬→구글, 구글→로컬 계정 병합
  - Curator(VC)는 별도 API를 통해 계정 발급
2. **로그인**
  - JWT 토큰 발급, 재발급
3. **공고 목록 보기**
  - 기본은 최신순 정렬, 페이지네이션 적용
  - 직군, 모집상태, 시리즈, 투자금액 필터링 + 최신순, 마감임박순 정렬
4. **공고 상세 페이지 보기**
  - 회사(Company) 글과 직군(Position)이 결합된 공고(Post) 형태로 표시
  - 공고목록에서는 보이지 않는 상세 공고글, 해시태그, 링크 등이 표시
5. **커피챗 신청(지원자)**
  - 전화번호와 내용을 작성해 입력하면 해당 커피챗 내용이 회사 이메일로 발송됨
6. **회사, 공고 글 작성(VC)**
  - 회사에 대한 정보를 작성
  - 해당 회사에 대한 공고 글을 여러 개 작성 가능
7. **마이페이지**
  - 지원자는 자신이 작성한 커피챗을 확인 가능(페이지네이션 적용)
  - VC는 자신이 작성한 회사, 공고글을 각각 확인 가능(페이지네이션 적용)

## 🛠️ 주요 기능 구현

### Access, Refresh Token

- JWT 토큰 기반
- Access 토큰은 1시간, Refresh Token은 7일의 유효기간
- Refresh Token은 Redis에 유저 ID 쌍으로 저장, 동일 유저가 재발급 할 때마다 refresh token을 삭제하고 다시 발급하는 RTR 방식 적용

### Email

- Gmail SMTP 이용(앱 비밀번호 기반)
- 스누메일 인증에 사용되는 코드는 Redis에 저장(3분의 유효기간)

### 공고 필터링

- JPA의 Specification API 이용
- 각 필터를 개별 함수를 통해 Predicate 리스트로 관리
- 최종적으로 Specification을 JPA 쿼리에 적용

### Exception Handling

- 각 에러상황에 대한 코드, 메시지를 별도 Enum으로 관리
- 각 Exception을 에러상황에 맞는 클래스로 처리


## 🔥 우리 조의 자랑할 거리

- 데일리 스크림을 통한 진행상황 공유, 동기화
- 이슈 기반 브랜치로 명확한 작업 계획, 수행

## 🏗️ ERD
![ERD](https://github.com/user-attachments/assets/a91ffb95-3cc7-4c0d-ad05-5f4bf049b84e)

## 📜 API 문서

- [API 문서 보기](https://api.survey-josha.site/swagger-ui/index.html#/)

## 🐛 버그 리포트 및 제안

버그를 발견하거나 새로운 기능을 제안하려면 [이슈 페이지](https://github.com/wafflestudio/22-5-team1-server/issues)를 이용해 주세요.

