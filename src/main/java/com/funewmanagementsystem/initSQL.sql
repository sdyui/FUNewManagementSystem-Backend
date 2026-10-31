-- Select database
USE SBA_ASSIGNMENT;
GO

-- =========================================================================
-- 0. FIX HIBERNATE ENUM CHECK CONSTRAINTS (IF OUTDATED)
-- Hibernate ddl-auto=update does not update existing CHECK constraints when
-- new enum values (like EDITOR, CONTRIBUTOR) are added to Java enums.
-- =========================================================================
DECLARE @sql NVARCHAR(MAX) = N'';

SELECT @sql += N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id))
    + N'.' + QUOTENAME(OBJECT_NAME(parent_object_id))
    + N' DROP CONSTRAINT ' + QUOTENAME(name) + N';' + CHAR(13)
FROM sys.check_constraints
WHERE parent_object_id IN (
    OBJECT_ID('dbo.system_account'),
    OBJECT_ID('dbo.news_article'),
    OBJECT_ID('dbo.article_workflow_log')
);

IF @sql <> N''
    EXEC sp_executesql @sql;
GO

-- Re-add updated CHECK constraint for system_account.account_role
ALTER TABLE dbo.system_account
ADD CONSTRAINT CK_system_account_role
CHECK (account_role IN ('ADMIN', 'STAFF', 'EDITOR', 'CONTRIBUTOR'));
GO

-- =========================================================================
-- 1. SEED SYSTEM ACCOUNTS (ADMIN, EDITOR, STAFF, CONTRIBUTOR)
-- =========================================================================

IF NOT EXISTS (SELECT 1 FROM system_account WHERE account_name = 'admin')
BEGIN
    INSERT INTO system_account (account_name, account_email, account_password, account_role)
    VALUES ('admin', 'admin@gmail.com', '$2a$10$E8yZZmh/JCV5q3GASBoGR.a.zOsRG0uAsNzN4fUF.JjgwzuwVMcyy', 'ADMIN');
END

IF NOT EXISTS (SELECT 1 FROM system_account WHERE account_name = 'editor1')
BEGIN
    INSERT INTO system_account (account_name, account_email, account_password, account_role)
    VALUES ('editor1', 'editor1@fpt.edu.vn', '$2a$10$E8yZZmh/JCV5q3GASBoGR.a.zOsRG0uAsNzN4fUF.JjgwzuwVMcyy', 'EDITOR');
END

IF NOT EXISTS (SELECT 1 FROM system_account WHERE account_name = 'staff1')
BEGIN
    INSERT INTO system_account (account_name, account_email, account_password, account_role)
    VALUES ('staff1', 'staff1@fpt.edu.vn', '$2a$10$E8yZZmh/JCV5q3GASBoGR.a.zOsRG0uAsNzN4fUF.JjgwzuwVMcyy', 'STAFF');
END

IF NOT EXISTS (SELECT 1 FROM system_account WHERE account_name = 'contributor1')
BEGIN
    INSERT INTO system_account (account_name, account_email, account_password, account_role)
    VALUES ('contributor1', 'contributor1@fpt.edu.vn', '$2a$10$E8yZZmh/JCV5q3GASBoGR.a.zOsRG0uAsNzN4fUF.JjgwzuwVMcyy', 'CONTRIBUTOR');
END

IF NOT EXISTS (SELECT 1 FROM system_account WHERE account_name = 'contributor2')
BEGIN
    INSERT INTO system_account (account_name, account_email, account_password, account_role)
    VALUES ('contributor2', 'contributor2@fpt.edu.vn', '$2a$10$E8yZZmh/JCV5q3GASBoGR.a.zOsRG0uAsNzN4fUF.JjgwzuwVMcyy', 'CONTRIBUTOR');
END
GO

-- =========================================================================
-- 2. RESET & SEED DATA
-- =========================================================================

-- Clear dependent tables first to avoid foreign key conflicts
IF OBJECT_ID('article_workflow_log', 'U') IS NOT NULL
    DELETE FROM article_workflow_log;
DELETE FROM news_tag;
DELETE FROM news_article;
DELETE FROM tag;
DELETE FROM category;
GO

-- =========================================================================
-- 3. CATEGORIES (Parent - Child Hierarchical Structure)
-- =========================================================================

SET IDENTITY_INSERT category ON;
GO

INSERT INTO category (categoryid, category_name, category_description, is_active, parent_categoryid) VALUES
-- Root Parent Categories (IDs 1 -> 5, parent_categoryid = NULL)
(1, N'Campus News', N'University news, official announcements, and campus events', 1, NULL),
(2, N'Academics & Admissions', N'Curriculum, academic policies, degree programs, and admissions', 1, NULL),
(3, N'Student Life & Activities', N'Student union campaigns, extracurricular activities, and student clubs', 1, NULL),
(4, N'Technology & Innovation', N'Tech news, software development, artificial intelligence, and research', 1, NULL),
(5, N'Archived (Inactive)', N'Outdated categories no longer in use (for testing inactive filter)', 0, NULL),

-- Sub-categories under ID = 1 (Campus News)
(6, N'Exams & Course Registration', N'Announcements regarding exam schedules and semester course registrations', 1, 1),
(7, N'Scholarships & Awards', N'Information on merit-based scholarships, financial aid, and awards', 1, 1),

-- Sub-categories under ID = 2 (Academics & Admissions)
(8, N'Undergraduate Admissions', N'Admission guidelines, criteria, deadlines, and intake quotas', 1, 2),
(9, N'International Exchange', N'Study abroad opportunities, partner universities, and student exchange', 1, 2),

-- Sub-categories under ID = 3 (Student Life & Activities)
(10, N'Clubs & Student Societies', N'Recruitment and activities of academic, cultural, and volunteer clubs', 1, 3),
(11, N'Sports & E-Sports', N'Campus sports tournaments, football, table tennis, and e-sports leagues', 1, 3),

-- Sub-categories under ID = 4 (Technology & Innovation)
(12, N'Artificial Intelligence (AI)', N'Research and applications in Machine Learning, Generative AI, and NLP', 1, 4),
(13, N'Software Engineering', N'Software architecture, web development, cloud computing, and DevOps', 1, 4);
GO

SET IDENTITY_INSERT category OFF;
GO

-- =========================================================================
-- 4. TAGS
-- =========================================================================

INSERT INTO tag (tag_name, note) VALUES 
('AI', N'Artificial Intelligence and Machine Learning'),
('DevOps', N'Software Development, CI/CD, and IT Operations'),
('Academics', N'Academic regulations, coursework, and study guides'),
('CampusLife', N'Student events, campus festivals, and club activities'),
('Scholarship', N'Scholarship announcements and financial support');
GO

-- =========================================================================
-- 5. NEWS ARTICLES (Article Management + Public News)
-- =========================================================================

DECLARE @AdminID BIGINT = (SELECT TOP 1 accountid FROM system_account WHERE account_name = 'admin');
DECLARE @Editor1ID BIGINT = ISNULL((SELECT TOP 1 accountid FROM system_account WHERE account_name = 'editor1'), @AdminID);
DECLARE @Contributor1ID BIGINT = ISNULL((SELECT TOP 1 accountid FROM system_account WHERE account_name = 'contributor1'), @AdminID);
DECLARE @Contributor2ID BIGINT = ISNULL((SELECT TOP 1 accountid FROM system_account WHERE account_name = 'contributor2'), @AdminID);

-- Article 1: PUBLISHED (Created by Admin, Category 1 - Campus News)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'University Welcomes New Students for Academic Year 2026 - 2027',
    N'Over 5,000 freshmen officially join the university community',
    N'The opening ceremony was held with great enthusiasm and solemnity, attended by distinguished university leaders, faculty members, and thousands of energetic new students...',
    N'Office of Student Affairs',
    N'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=800',
    1, 'PUBLISHED', DATEADD(DAY, -10, GETDATE()), DATEADD(DAY, -9, GETDATE()), DATEADD(DAY, -9, GETDATE()),
    1, @AdminID, @AdminID, 0
);

-- Article 2: PUBLISHED (Created by Contributor 1, Approved by Editor 1, Category 12 - AI)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'Workshop: Generative AI and Modern Software Engineering Trends',
    N'Industry experts discuss AI agent workflows and digital transformation in 2026',
    N'The workshop featured comprehensive demonstrations on practical machine learning integration, LLM fine-tuning, and automated cloud workflows...',
    N'Faculty of Information Technology',
    N'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800',
    1, 'PUBLISHED', DATEADD(DAY, -8, GETDATE()), DATEADD(DAY, -7, GETDATE()), DATEADD(DAY, -7, GETDATE()),
    12, @Contributor1ID, @Editor1ID, 0
);

-- Article 3: PUBLISHED (Created by Admin, Category 11 - Sports & E-Sports)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'Annual Campus Football Championship & E-Sports Tournament Kicks Off',
    N'More than 32 teams compete for the prestigious championship trophy',
    N'The annual sports tournament officially began this weekend with passionate matches, high team spirit, and energetic cheers from thousands of student spectators...',
    N'Campus Sports Committee',
    N'https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800',
    1, 'PUBLISHED', DATEADD(DAY, -6, GETDATE()), DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -5, GETDATE()),
    11, @AdminID, @AdminID, 0
);

-- Article 4: DRAFT (Created by Contributor 1 - Can test Edit & Submit for Review)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'Guide to Spring Boot 3 & Microservices Architecture for Students',
    N'Step-by-step tutorial on building scalable backend systems',
    N'In this draft article, we explore how students can structure their SBA301 projects using Spring Boot, Spring Security with JWT, and SQL Server best practices...',
    N'SE Student Club',
    N'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800',
    0, 'DRAFT', DATEADD(DAY, -3, GETDATE()), NULL, NULL,
    13, @Contributor1ID, NULL, 0
);

-- Article 5: CHANGES_REQUESTED (Created by Contributor 1 - Can test Edit & Resubmit)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'FPT Merit Scholarship Application Guidelines for Fall Semester',
    N'Detailed requirements and interview tips for scholarship candidates',
    N'Students applying for the merit scholarship need to prepare their academic transcripts, recommendation letters, and personal statements before the deadline...',
    N'Financial Aid Office',
    N'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800',
    0, 'CHANGES_REQUESTED', DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -2, GETDATE()), NULL,
    7, @Contributor1ID, @Editor1ID, 0
);

-- Article 6: PENDING_REVIEW (Created by Contributor 1 - Waiting for Editor approval, cannot be edited by Contributor)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'International Student Exchange Program with Partner Universities in Japan',
    N'Registration is now open for 6-month exchange semester in Tokyo',
    N'The International Relations Office announces 50 slots for IT and Business students to participate in the upcoming cultural and academic exchange program...',
    N'International Office',
    N'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800',
    0, 'PENDING_REVIEW', DATEADD(DAY, -2, GETDATE()), DATEADD(DAY, -1, GETDATE()), NULL,
    9, @Contributor1ID, @Contributor1ID, 0
);

-- Article 7: SCHEDULED (Created by Contributor 2, Scheduled by Editor 1)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'Final Examination Schedule & Rules for Semester 2026',
    N'Important reminders on exam room regulations and student ID verification',
    N'All students must arrive at least 15 minutes prior to the examination time and bring their valid student ID cards...',
    N'Academic Affairs',
    N'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800',
    0, 'SCHEDULED', DATEADD(DAY, -4, GETDATE()), DATEADD(DAY, -1, GETDATE()), DATEADD(DAY, 3, GETDATE()),
    6, @Contributor2ID, @Editor1ID, 0
);

-- Article 8: DRAFT (Created by Contributor 2 - For testing ownership permission check)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'Top 5 Student Clubs to Join During Your Freshman Year',
    N'Discover music, coding, photography, and volunteer communities on campus',
    N'Joining a student club is one of the best ways to build soft skills, make lifelong friends, and balance academic pressure...',
    N'Student Union',
    N'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800',
    0, 'DRAFT', DATEADD(DAY, -1, GETDATE()), NULL, NULL,
    10, @Contributor2ID, NULL, 0
);

-- Article 9: ARCHIVED (Created by Admin)
INSERT INTO news_article (
    news_title, headline, news_content, news_source, thumb_nail,
    news_status, article_status, created_date, modified_date, publish_date,
    categoryid, created_byid, updated_byid, version
)
VALUES (
    N'Summer 2025 Course Registration Notice (Archived)',
    N'Archived notice from previous academic year',
    N'This announcement has expired and is archived for historical reference only.',
    N'Academic Affairs',
    N'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800',
    0, 'ARCHIVED', DATEADD(DAY, -30, GETDATE()), DATEADD(DAY, -15, GETDATE()), DATEADD(DAY, -29, GETDATE()),
    6, @AdminID, @AdminID, 0
);

-- =========================================================================
-- 6. NEWS TAG MAPPINGS
-- =========================================================================

DECLARE @Article1ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%University Welcomes%');
DECLARE @Article2ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%Workshop: Generative AI%');
DECLARE @Article3ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%Annual Campus Football%');
DECLARE @Article4ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%Guide to Spring Boot 3%');
DECLARE @Article5ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%FPT Merit Scholarship%');
DECLARE @Article6ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%International Student Exchange%');
DECLARE @Article7ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%Final Examination Schedule%');
DECLARE @Article8ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%Top 5 Student Clubs%');

DECLARE @TagCampusLife BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'CampusLife');
DECLARE @TagAI BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'AI');
DECLARE @TagDevOps BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'DevOps');
DECLARE @TagAcademics BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'Academics');
DECLARE @TagScholarship BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'Scholarship');

IF @Article1ID IS NOT NULL AND @TagCampusLife IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article1ID, @TagCampusLife);

IF @Article2ID IS NOT NULL AND @TagAI IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article2ID, @TagAI);

IF @Article2ID IS NOT NULL AND @TagDevOps IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article2ID, @TagDevOps);

IF @Article3ID IS NOT NULL AND @TagCampusLife IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article3ID, @TagCampusLife);

IF @Article4ID IS NOT NULL AND @TagDevOps IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article4ID, @TagDevOps);

IF @Article5ID IS NOT NULL AND @TagScholarship IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article5ID, @TagScholarship);

IF @Article6ID IS NOT NULL AND @TagAcademics IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article6ID, @TagAcademics);

IF @Article7ID IS NOT NULL AND @TagAcademics IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article7ID, @TagAcademics);

IF @Article8ID IS NOT NULL AND @TagCampusLife IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article8ID, @TagCampusLife);

-- =========================================================================
-- 7. ARTICLE WORKFLOW LOGS (Audit History for Article Management)
-- =========================================================================

IF OBJECT_ID('article_workflow_log', 'U') IS NOT NULL
BEGIN
    -- Workflow history for Article 2 (DRAFT -> PENDING_REVIEW -> PUBLISHED)
    IF @Article2ID IS NOT NULL
    BEGIN
        INSERT INTO article_workflow_log (news_articleid, from_status, to_status, actorid, note, action_at)
        VALUES
        (@Article2ID, 'DRAFT', 'PENDING_REVIEW', @Contributor1ID, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -8, GETDATE())),
        (@Article2ID, 'PENDING_REVIEW', 'PUBLISHED', @Editor1ID, N'Nội dung tốt, duyệt xuất bản', DATEADD(DAY, -7, GETDATE()));
    END

    -- Workflow history for Article 5 (DRAFT -> PENDING_REVIEW -> CHANGES_REQUESTED)
    IF @Article5ID IS NOT NULL
    BEGIN
        INSERT INTO article_workflow_log (news_articleid, from_status, to_status, actorid, note, action_at)
        VALUES
        (@Article5ID, 'DRAFT', 'PENDING_REVIEW', @Contributor1ID, N'Gửi bài viết hướng dẫn học bổng', DATEADD(DAY, -4, GETDATE())),
        (@Article5ID, 'PENDING_REVIEW', 'CHANGES_REQUESTED', @Editor1ID, N'Vui lòng bổ sung thêm hạn nộp hồ sơ cụ thể cho từng cơ sở', DATEADD(DAY, -2, GETDATE()));
    END

    -- Workflow history for Article 6 (DRAFT -> PENDING_REVIEW)
    IF @Article6ID IS NOT NULL
    BEGIN
        INSERT INTO article_workflow_log (news_articleid, from_status, to_status, actorid, note, action_at)
        VALUES
        (@Article6ID, 'DRAFT', 'PENDING_REVIEW', @Contributor1ID, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -1, GETDATE()));
    END
END
GO
