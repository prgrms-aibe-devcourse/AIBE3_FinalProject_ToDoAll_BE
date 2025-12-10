-- 채용공고 (JD)
INSERT INTO job_descriptions (id, title, department, work_type, experience, education, salary, description, start_date, deadline, status, welfare, location, thumbnail_url, applicant_count, author_id, created_at, updated_at)
VALUES (
           2001, 'AI 백엔드 엔지니어', 'AI 개발팀', '정규직', '3년 이상', '학사 이상', '연 5,000만원 ~ 7,000만원',
           'Java, Spring Boot, Python, Elasticsearch, Kafka, Redis 등의 기술 스택을 활용한 대규모 트래픽 백엔드 서비스 개발. AI 기반 추천 시스템 경험 우대.',
           '2025-11-20', '2025-12-31', 'OPEN', '유연 근무제, 재택 가능, 최신 장비 제공',
           '서울특별시 강남구 테헤란로', 'https://example.com/job2001-thumbnail.png', 0, 1, NOW(), NOW()
       );

-- 스킬목록
INSERT INTO skills (id, name) VALUES
                                  (1, 'spring'),
                                  (2, 'jpa'),
                                  (3, 'redis'),
                                  (4, 'react'),
                                  (5, 'node.js'),
                                  (6, 'mongodb'),
                                  (7, 'python'),
                                  (8, 'django'),
                                  (9, 'postgresql'),
                                  (10, 'java'),
                                  (11, 'kafka'),
                                  (12, 'mysql'),
                                  (13, 'oracle'),
                                  (14, 'docker'),
                                  (15, 'aws'),
                                  (16, 'go'),
                                  (17, 'grpc'),
                                  (18, '.net'),
                                  (19, 'c#'),
                                  (20, 'mssql'),
                                  (21, 'swift'),
                                  (22, 'ios'),
                                  (23, 'firebase'),
                                  (24, 'git'),
                                  (25, 'typescript'),
                                  (26, 'vue.js'),
                                  (27, 'angular');

-- 필수 스킬 (Required)
INSERT INTO job_required_skills (job_id, skill_id) VALUES (2001, 1);  -- spring
INSERT INTO job_required_skills (job_id, skill_id) VALUES (2001, 2);  -- jpa
INSERT INTO job_required_skills (job_id, skill_id) VALUES (2001, 3);  -- redis
INSERT INTO job_required_skills (job_id, skill_id) VALUES (2001, 10); -- java
INSERT INTO job_required_skills (job_id, skill_id) VALUES (2001, 12); -- mysql

-- 우대 스킬 (Preferred)
INSERT INTO job_preferred_skills (job_id, skill_id) VALUES (2001, 7);  -- python
INSERT INTO job_preferred_skills (job_id, skill_id) VALUES (2001, 11); -- kafka
INSERT INTO job_preferred_skills (job_id, skill_id) VALUES (2001, 14); -- docker
INSERT INTO job_preferred_skills (job_id, skill_id) VALUES (2001, 15); -- aws


-- 이력서 정보
INSERT INTO resumes (id, name, gender, birth_date, email, address, status, jd_id)
VALUES
    (3000, '김민준', '남성', '1992-05-14', 'minjun.kim@example.com', '서울특별시 마포구', 'NEW', 2001),
    (3001, '이서연', '여성', '1993-08-22', 'seoyeon.lee@example.com', '서울특별시 성동구', 'NEW', 2001),
    (3002, '박지훈', '남성', '1991-12-02', 'jihoon.park@example.com', '서울특별시 강서구', 'NEW', 2001),
    (3003, '최유진', '여성', '1994-01-30', 'yujin.choi@example.com', '서울특별시 송파구', 'NEW', 2001),
    (3004, '정우성', '남성', '1990-03-10', 'woosung.jung@example.com', '서울특별시 동작구', 'NEW', 2001),
    (3005, '한지민', '여성', '1992-09-18', 'jimin.han@example.com', '서울특별시 강남구', 'NEW', 2001),
    (3006, '서강준', '남성', '1993-11-05', 'kangjoon.seo@example.com', '서울특별시 은평구', 'NEW', 2001),
    (3007, '배수지', '여성', '1995-07-11', 'suzy.bae@example.com', '서울특별시 노원구', 'NEW', 2001),
    (3008, '강다니엘', '남성', '1991-10-23', 'daniel.kang@example.com', '서울특별시 관악구', 'NEW', 2001),
    (3009, '문채원', '여성', '1994-12-15', 'chaewon.moon@example.com', '서울특별시 종로구', 'NEW', 2001),
    (3010, '오지은', '남성', '1990-02-25', 'js.park@example.com', '서울 강남구', 'NEW', 2001),
    (3011, '김연아', '여성', '1992-09-05', 'yuna.kim@example.com', '서울 송파구', 'NEW', 2001),
    (3012, '이강운', '남성', '1995-01-15', 'gi.lee@example.com', '서울 서초구', 'NEW', 2001),
    (3013, '최수진', '여성', '1991-12-10', 'sj.choi@example.com', '서울 마포구', 'NEW', 2001),
    (3014, '정민호', '남성', '1989-07-08', 'mh.jeong@example.com', '서울 관악구', 'NEW', 2001),
    (3015, '오하늘', '여성', '1993-04-18', 'hn.oh@example.com', '서울 노원구', 'NEW', 2001),
    (3016, '한지은', '여성', '1994-06-12', 'je.han@example.com', '서울 성북구', 'NEW', 2001),
    (3017, '장호석', '남성', '1990-10-30', 'dg.jang@example.com', '서울 동작구', 'NEW', 2001),
    (3018, '이보영', '여성', '1991-05-14', 'by.lee@example.com', '서울 강서구', 'NEW', 2001),
    (3019, '김태희', '여성', '1992-11-09', 'th.kim@example.com', '서울 중랑구', 'NEW', 2001);

-- 학력 정보
INSERT INTO resume_educations (resume_id, education_level, school_name, major, is_graduated, admission_date, graduation_date, attendance_type, gpa, gpa_scale)
VALUES
    (3000, 'UNIVERSITY_ABOVE', '서울대학교', '컴퓨터공학과', true, '2011-03-01', '2015-02-28', 'DAY', 4.04, 4.5),
    (3001, 'UNIVERSITY_ABOVE', '연세대학교', '정보통신공학과', true, '2010-03-01', '2014-02-28', 'DAY', 3.89, 4.5),
    (3002, 'UNIVERSITY_ABOVE', '고려대학교', '소프트웨어학과', true, '2012-03-01', '2016-02-28', 'DAY', 3.71, 4.5),
    (3003, 'UNIVERSITY_ABOVE', '성균관대학교', '전자전기공학부', true, '2011-03-01', '2015-02-28', 'DAY', 3.55, 4.5),
    (3004, 'UNIVERSITY_ABOVE', '한양대학교', '기계공학부', true, '2010-03-01', '2014-02-28', 'DAY', 3.44, 4.5),
    (3005, 'UNIVERSITY_ABOVE', '이화여자대학교', '컴퓨터공학과', true, '2011-03-01', '2015-02-28', 'DAY', 3.64, 4.5),
    (3006, 'UNIVERSITY_ABOVE', '중앙대학교', '산업경영공학과', true, '2012-03-01', '2016-02-28', 'DAY', 3.81, 4.5),
    (3007, 'UNIVERSITY_ABOVE', '홍익대학교', '디자인학부', true, '2011-03-01', '2015-02-28', 'DAY', 4.06, 4.5),
    (3008, 'UNIVERSITY_ABOVE', '건국대학교', '컴퓨터공학과', true, '2010-03-01', '2014-02-28', 'DAY', 3.45, 4.5),
    (3009, 'UNIVERSITY_ABOVE', '경희대학교', '정보디스플레이학과', true, '2011-03-01', '2015-02-28', 'DAY', 3.67, 4.5),
    (3010, 'UNIVERSITY_ABOVE', '인하대학교', '컴퓨터공학과', true, '2009-03-01', '2013-02-28', 'DAY', 4.1, 4.5),
    (3011, 'UNIVERSITY_ABOVE', '아주대학교', '소프트웨어학과', true, '2011-03-01', '2015-02-28', 'DAY', 3.9, 4.5),
    (3012, 'UNIVERSITY_ABOVE', '숭실대학교', '정보보호학과', true, '2012-03-01', '2016-02-28', 'DAY', 4.2, 4.5),
    (3013, 'UNIVERSITY_ABOVE', '서울과학기술대학교', '산업공학과', true, '2008-03-01', '2012-02-28', 'DAY', 3.6, 4.5),
    (3014, 'UNIVERSITY_ABOVE', '성균관대학교', '전산학과', true, '2010-03-01', '2014-02-28', 'DAY', 3.95, 4.5),
    (3015, 'UNIVERSITY_ABOVE', '성신여자대학교', 'AI융합학과', true, '2013-03-01', '2017-02-28', 'DAY', 4.3, 4.5),
    (3016, 'UNIVERSITY_ABOVE', '인천대학교', '통계학과', true, '2009-03-01', '2013-02-28', 'DAY', 3.85, 4.5),
    (3017, 'UNIVERSITY_ABOVE', '경북대학교', '물리학과', true, '2011-03-01', '2015-02-28', 'DAY', 3.4, 4.5),
    (3018, 'UNIVERSITY_ABOVE', '부산대학교', '전자공학과', true, '2007-03-01', '2011-02-28', 'DAY', 3.7, 4.5),
    (3019, 'UNIVERSITY_ABOVE', 'KAIST', 'AI학과', true, '2010-03-01', '2014-02-28', 'DAY', 4.4, 4.5);

-- 경력 정보
INSERT INTO resume_experiences (resume_id, company_name, department, position, start_date, end_date) VALUES
                                                                                                         (3000, '카카오모빌리티', 'AI 개발팀', '백엔드 엔지니어', '2018-01-01', '2022-12-31'),
                                                                                                         (3001, '네이버페이', '플랫폼개발팀', '서버 개발자', '2017-03-01', '2021-11-30'),
                                                                                                         (3002, '라인플러스', 'AI 서비스팀', '데이터 엔지니어', '2019-06-01', '2023-06-30'),
                                                                                                         (3003, '배달의민족', '데이터팀', 'ML 엔지니어', '2016-09-01', '2020-08-31'),
                                                                                                         (3004, '현대모비스', '제조IT팀', 'IoT 엔지니어', '2015-01-01', '2019-12-31'),
                                                                                                         (3005, '삼성전자', '소프트웨어센터', '백엔드 개발자', '2017-02-01', '2021-07-31'),
                                                                                                         (3006, 'LG CNS', '스마트팩토리팀', '임베디드 개발자', '2018-04-01', '2022-03-31'),
                                                                                                         (3007, '한화시스템', 'UX개발팀', '프론트엔드 개발자', '2019-01-01', '2023-01-31'),
                                                                                                         (3008, '슈어소프트', '기술팀', '풀스택 개발자', '2020-01-01', '2024-01-31'),
                                                                                                         (3009, '디아이', '정보시스템팀', '시스템 관리자', '2016-05-01', '2020-04-30'),
                                                                                                         (3010, '롯데정보통신', '클라우드개발팀', 'DevOps 엔지니어', '2019-02-01', '2023-12-31'),
                                                                                                         (3011, 'NHN', '데이터분석팀', '데이터 분석가', '2018-01-01', '2022-11-30'),
                                                                                                         (3012, 'CJ올리브네트웍스', '모바일플랫폼팀', 'Android 개발자', '2017-06-01', '2021-05-31'),
                                                                                                         (3013, 'KT', 'AI 서비스 개발팀', 'AI 백엔드 엔지니어', '2020-03-01', '2024-01-31'),
                                                                                                         (3014, '삼성SDS', 'ERP개발팀', 'SAP 컨설턴트', '2016-01-01', '2020-01-01'),
                                                                                                         (3015, '스타트업C', '기술팀', '데이터 엔지니어', '2018-05-01', '2022-05-01'),
                                                                                                         (3016, '다날', '핀테크개발팀', '백엔드 개발자', '2019-04-01', '2023-03-31'),
                                                                                                         (3017, '넷마블', '게임개발팀', '게임 서버 개발자', '2017-01-01', '2021-01-01'),
                                                                                                         (3018, '현대오토에버', '차량IT팀', 'IoT 시스템 엔지니어', '2015-03-01', '2019-03-01'),
                                                                                                         (3019, '쿠팡', '인프라팀', '시스템 엔지니어', '2018-06-01', '2022-06-01');

-- 이력서에 작성된 스킬 목록
INSERT INTO resume_skills (resume_id, skill_id, proficiency_level) VALUES
                                                                       (3000, 1, 'EXPERT'), (3000, 10, 'EXPERT'), (3000, 3, 'INTERMEDIATE'),  -- spring, java, redis
                                                                       (3001, 2, 'INTERMEDIATE'), (3001, 12, 'INTERMEDIATE'), (3001, 14, 'BEGINNER'),  -- jpa, mysql, docker
                                                                       (3002, 7, 'EXPERT'), (3002, 11, 'INTERMEDIATE'), (3002, 15, 'INTERMEDIATE'),  -- python, kafka, aws
                                                                       (3003, 1, 'INTERMEDIATE'), (3003, 3, 'INTERMEDIATE'), (3003, 11, 'BEGINNER'),  -- spring, redis, kafka
                                                                       (3004, 13, 'INTERMEDIATE'), (3004, 6, 'BEGINNER'), (3004, 16, 'INTERMEDIATE'), -- oracle, mongodb, go
                                                                       (3005, 1, 'EXPERT'), (3005, 10, 'EXPERT'), (3005, 2, 'EXPERT'), -- spring, java, jpa
                                                                       (3006, 14, 'INTERMEDIATE'), (3006, 15, 'INTERMEDIATE'), (3006, 3, 'BEGINNER'), -- docker, aws, redis
                                                                       (3007, 4, 'INTERMEDIATE'), (3007, 25, 'INTERMEDIATE'), (3007, 24, 'EXPERT'), -- react, typescript, git
                                                                       (3008, 5, 'INTERMEDIATE'), (3008, 6, 'INTERMEDIATE'), (3008, 10, 'BEGINNER'), -- node.js, mongodb, java
                                                                       (3009, 7, 'EXPERT'), (3009, 8, 'EXPERT'), (3009, 9, 'INTERMEDIATE'), -- python, django, postgresql
                                                                       (3010, 14, 'EXPERT'), (3010, 15, 'INTERMEDIATE'), (3010, 3, 'INTERMEDIATE'),
                                                                       (3011, 7, 'EXPERT'), (3011, 11, 'INTERMEDIATE'), (3011, 13, 'BEGINNER'),
                                                                       (3012, 5, 'INTERMEDIATE'), (3012, 17, 'INTERMEDIATE'), (3012, 24, 'BEGINNER'),
                                                                       (3013, 1, 'EXPERT'), (3013, 2, 'INTERMEDIATE'), (3013, 10, 'EXPERT'),
                                                                       (3014, 22, 'EXPERT'), (3014, 13, 'INTERMEDIATE'), (3014, 6, 'BEGINNER'),
                                                                       (3015, 7, 'INTERMEDIATE'), (3015, 8, 'EXPERT'), (3015, 9, 'INTERMEDIATE'),
                                                                       (3016, 1, 'INTERMEDIATE'), (3016, 12, 'INTERMEDIATE'), (3016, 3, 'INTERMEDIATE'),
                                                                       (3017, 10, 'EXPERT'), (3017, 16, 'INTERMEDIATE'), (3017, 18, 'BEGINNER'),
                                                                       (3018, 19, 'INTERMEDIATE'), (3018, 6, 'INTERMEDIATE'), (3018, 20, 'BEGINNER'),
                                                                       (3019, 14, 'INTERMEDIATE'), (3019, 21, 'INTERMEDIATE'), (3019, 3, 'INTERMEDIATE');


-- 자격,어학 등 정보
INSERT INTO resume_certifications (resume_id, type, name, score_or_level) VALUES
                                                                              (3000, 'LICENSE', '정보처리기사', '합격'),
                                                                              (3001, 'LANGUAGE', 'TOEIC', '870'),
                                                                              (3002, 'LICENSE', 'SQLD', '취득'),
                                                                              (3003, 'LANGUAGE', 'OPIc', 'IH'),
                                                                              (3004, 'LICENSE', 'ADsP', '취득'),
                                                                              (3005, 'LANGUAGE', 'TOEFL', '94'),
                                                                              (3006, 'LICENSE', '정보보안기사', '합격'),
                                                                              (3007, 'LICENSE', '리눅스마스터 2급', '합격'),
                                                                              (3008, 'LICENSE', '네트워크관리사 2급', '합격'),
                                                                              (3009, 'LANGUAGE', 'TOEIC Speaking', 'Level 7'),
                                                                              (3010, 'LICENSE', 'AWS Certified Solutions Architect', 'Associate'),
                                                                              (3011, 'LANGUAGE', 'TOEIC', '930'),
                                                                              (3012, 'LICENSE', '정보처리산업기사', '합격'),
                                                                              (3013, 'LICENSE', '빅데이터분석기사', '취득'),
                                                                              (3014, 'LICENSE', 'SAP FI 자격증', '취득'),
                                                                              (3015, 'LANGUAGE', 'TOEIC Speaking', 'Level 6'),
                                                                              (3016, 'LICENSE', 'Kotlin 개발자 인증', '합격'),
                                                                              (3017, 'LICENSE', '게임 기획 전문가', '취득'),
                                                                              (3018, 'LICENSE', 'IoT 전문가 자격증', '합격'),
                                                                              (3019, 'LICENSE', '리눅스 마스터 1급', '합격');

-- 관련 활동 및 수상 정보
INSERT INTO resume_activities (resume_id, title, type, organization) VALUES
                                                                         (3000, 'GitHub Contributor', 'ACTIVITY', 'TensorFlow 팀'),
                                                                         (3001, 'AI 해커톤 수상', 'ACTIVITY', 'Korea AI Hackathon'),
                                                                         (3002, '블로그 서비스 운영', 'PROJECT', '개인 프로젝트'),
                                                                         (3003, '기술 블로그 운영', 'ACTIVITY', 'Tistory Blog'),
                                                                         (3004, '모각코 운영진', 'ACTIVITY', 'Seoul Developers'),
                                                                         (3005, 'SpringWorld 발표자', 'EDUCATION', 'SpringWorld 컨퍼런스'),
                                                                         (3006, 'Kafka 오픈소스 기여', 'PROJECT', 'Apache Software Foundation'),
                                                                         (3007, 'UX/UI 해커톤 수상', 'ACTIVITY', 'Design Challenge Korea'),
                                                                         (3008, '스터디 플랫폼 개발', 'PROJECT', '팀 사이드 프로젝트'),
                                                                         (3009, 'GPT 스터디 리더', 'ACTIVITY', 'AI Study Club'),
                                                                         (3010, 'DevOps 세미나 참가', 'EDUCATION', 'AWS Korea'),
                                                                         (3011, 'SQL 시각화 발표', 'EDUCATION', 'Data Summit Korea'),
                                                                         (3012, '앱 배포 프로젝트', 'PROJECT', '개인 프로젝트'),
                                                                         (3013, '추천 시스템 개발', 'PROJECT', 'AI TF팀'),
                                                                         (3014, 'SAP 사용자 세션 발표', 'EDUCATION', 'SAP Korea'),
                                                                         (3015, '데이터 해커톤 참가', 'ACTIVITY', 'DataHack 2023'),
                                                                         (3016, 'Kubernetes 발표', 'EDUCATION', '사내 기술 세미나'),
                                                                         (3017, 'RPG 게임엔진 개발', 'PROJECT', '게임개발 동아리'),
                                                                         (3018, 'IoT 해커톤 우승', 'ACTIVITY', 'IoT Korea Hackathon'),
                                                                         (3019, '리눅스 커널 스터디', 'EDUCATION', 'Infra Study Group');