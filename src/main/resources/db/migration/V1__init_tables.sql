-- 1. 유저 (User)
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID (PK)',
                       email VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일 (로그인 ID)',
                       password VARCHAR(255) COMMENT '비밀번호 (암호화 저장)',
                       name VARCHAR(100) COMMENT '사용자 이름',
                       nickname VARCHAR(100) COMMENT '닉네임 (선택사항)',
                       phone_number VARCHAR(30) COMMENT '연락처',
                       birth_date DATE COMMENT '생년월일',
                       gender VARCHAR(10) COMMENT '성별 (남성/여성 등)',
                       company_name VARCHAR(255) COMMENT '회사명',
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 채용공고 (Job Description)
CREATE TABLE job_descriptions (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '공고 ID (PK)',
                                  title VARCHAR(255) NOT NULL COMMENT '공고 제목',
                                  department VARCHAR(255) COMMENT '부서명',
                                  work_type VARCHAR(100) COMMENT '근무 형태 (정규직, 계약직 등)',
                                  experience VARCHAR(100) COMMENT '경력 요건',
                                  education VARCHAR(100) COMMENT '학력 요건',
                                  salary VARCHAR(100) COMMENT '급여/연봉 정보',
                                  description TEXT COMMENT '업무 설명 (본문)',
                                  start_date DATE COMMENT '공고 시작일',
                                  deadline DATE COMMENT '공고 마감일',
                                  status ENUM('DRAFT', 'OPEN', 'CLOSED') DEFAULT 'DRAFT' COMMENT '공고 상태',
                                  required_skills JSON COMMENT '필수 기술 목록',
                                  preferred_skills JSON COMMENT '우대 기술 목록',
                                  welfare TEXT COMMENT '복리후생',
                                  applicant_count BIGINT DEFAULT 0 COMMENT '지원자 수',
                                  author_id BIGINT NOT NULL COMMENT '작성자 ID (users.id 참조)',
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                  CONSTRAINT fk_job_author FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 이력서 (Resume)
CREATE TABLE resumes (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력서 ID (PK)',
                         name VARCHAR(100) COMMENT '이름',
                         gender VARCHAR(10) COMMENT '성별',
                         birth_date DATE COMMENT '생년월일',
                         email VARCHAR(255) COMMENT '이메일',
                         phone VARCHAR(50) COMMENT '연락처',
                         address VARCHAR(255) COMMENT '주소',
                         detail_address VARCHAR(255) COMMENT '상세주소',
                         education JSON COMMENT '학력 정보',
                         experience JSON COMMENT '경력 정보',
                         skills JSON COMMENT '보유 스킬',
                         activities JSON COMMENT '경험/활동/교육',
                         certifications JSON COMMENT '자격/어학/수상 내역',
                         resume_file_url VARCHAR(500) COMMENT '이력서 파일 URL',
                         portfolio_file_url VARCHAR(500) COMMENT '포트폴리오 파일 URL',
                         status ENUM('NEW', 'BOOKMARK', 'HOLD', 'REJECT') DEFAULT 'NEW' COMMENT '이력서 상태',
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. JD - 이력서 매칭 (Match)
CREATE TABLE matches (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '매칭 ID (PK)',
                         jd_id BIGINT NOT NULL COMMENT '채용공고 ID (job_descriptions.id 참조)',
                         resume_id BIGINT NOT NULL COMMENT '이력서 ID (resumes.id 참조)',
                         applied_at DATETIME COMMENT '지원 일시',
                         match_score FLOAT COMMENT '매칭 점수',
                         recommendation_reason TEXT COMMENT '추천 사유 (AI 결과)',
                         status ENUM('RECOMMENDED', 'APPLIED', 'REJECTED') DEFAULT 'RECOMMENDED' COMMENT '매칭 상태',
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                         CONSTRAINT fk_match_jd FOREIGN KEY (jd_id) REFERENCES job_descriptions(id),
                         CONSTRAINT fk_match_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 면접 (Interview)
CREATE TABLE interviews (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '면접 ID (PK)',
                            jd_id BIGINT NOT NULL COMMENT '채용공고 ID',
                            resume_id BIGINT NOT NULL COMMENT '이력서 ID',
                            organizer_id BIGINT NOT NULL COMMENT '면접 생성자 ID (users.id 참조)',
                            scheduled_at DATETIME COMMENT '면접 예정 일시',
                            status ENUM('WAITING', 'IN_PROGRESS', 'DONE') DEFAULT 'WAITING' COMMENT '면접 상태',
                            summary TEXT COMMENT '면접 요약',
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                            CONSTRAINT fk_interview_jd FOREIGN KEY (jd_id) REFERENCES job_descriptions(id),
                            CONSTRAINT fk_interview_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
                            CONSTRAINT fk_interview_organizer FOREIGN KEY (organizer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 면접 참여자 (Interview Participant)
CREATE TABLE interview_participants (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '면접 참여자 ID (PK)',
                                        interview_id BIGINT NOT NULL COMMENT '면접 ID',
                                        user_id BIGINT NOT NULL COMMENT '참여자 ID',
                                        role VARCHAR(50) COMMENT '참여자 역할 (INTERVIEWER, OBSERVER)',
                                        joined_at DATETIME COMMENT '참여 시작 시간',
                                        left_at DATETIME COMMENT '참여 종료 시간',
                                        CONSTRAINT fk_participant_interview FOREIGN KEY (interview_id) REFERENCES interviews(id),
                                        CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 면접 질문 (Interview Question)
CREATE TABLE interview_questions (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '질문 ID (PK)',
                                     interview_id BIGINT NOT NULL COMMENT '면접 ID',
                                     type VARCHAR(50) COMMENT '질문 유형 (CORE, TECH, BEHAVIOR)',
                                     question_text TEXT COMMENT '질문 내용',
                                     answer TEXT COMMENT '답변 내용',
                                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                     CONSTRAINT fk_question_interview FOREIGN KEY (interview_id) REFERENCES interviews(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 면접 노트 (Interview Note)
CREATE TABLE interview_notes (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '면접 노트 ID (PK)',
                                 interview_id BIGINT NOT NULL COMMENT '면접 ID',
                                 author_id BIGINT NOT NULL COMMENT '작성자 ID (users.id 참조)',
                                 content TEXT COMMENT '노트 내용',
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                 updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                 CONSTRAINT fk_note_interview FOREIGN KEY (interview_id) REFERENCES interviews(id),
                                 CONSTRAINT fk_note_author FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 면접 평가 (Interview Evaluation)
CREATE TABLE interview_evaluations (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '면접 평가 ID (PK)',
                                       interview_id BIGINT NOT NULL COMMENT '면접 ID',
                                       evaluator_id BIGINT NOT NULL COMMENT '평가자 ID',
                                       score_tech INT COMMENT '기술 점수',
                                       score_comm INT COMMENT '커뮤니케이션 점수',
                                       score_overall INT COMMENT '종합 점수',
                                       comment TEXT COMMENT '평가 코멘트',
                                       result ENUM('PASS', 'HOLD', 'FAIL') COMMENT '면접 결과',
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                       CONSTRAINT fk_evaluation_interview FOREIGN KEY (interview_id) REFERENCES interviews(id),
                                       CONSTRAINT fk_evaluation_evaluator FOREIGN KEY (evaluator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 대시보드 (Dashboard)
CREATE TABLE dashboards (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '대시보드 통계 ID (PK)',
                            total_jobs INT DEFAULT 0 COMMENT '총 채용공고 수',
                            total_resumes INT DEFAULT 0 COMMENT '총 이력서 수',
                            total_matches INT DEFAULT 0 COMMENT '총 매칭 수',
                            total_interviews INT DEFAULT 0 COMMENT '총 면접 수',
                            completed_interviews INT DEFAULT 0 COMMENT '완료된 면접 수',
                            completed_hires INT DEFAULT 0 COMMENT '최종 채용 완료 수',
                            upcoming_interviews INT DEFAULT 0 COMMENT '다가오는 면접 수',
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;