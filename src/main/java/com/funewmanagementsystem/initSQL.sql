-- Select database
USE SBA_ASSIGNMENT;
GO

-- 1. Insert Default Admin Account (only if it does not exist)
IF NOT EXISTS (SELECT 1 FROM system_account WHERE account_name = 'admin')
BEGIN
    INSERT INTO system_account (
        account_name,
        account_email,
        account_password,
        account_role
    )
    VALUES (
        'admin',
        'admin@gmail.com',
        '$2a$10$E8yZZmh/JCV5q3GASBoGR.a.zOsRG0uAsNzN4fUF.JjgwzuwVMcyy',
        'ADMIN'
    );
END
GO

-- =========================================================================
-- RESET & SEED DATA IN ENGLISH
-- =========================================================================

-- Clear dependent tables first to avoid foreign key conflicts
DELETE FROM news_tag;
DELETE FROM news_article;
DELETE FROM tag;
DELETE FROM category;
GO

-- =========================================================================
-- CATEGORIES (Parent - Child Hierarchical Structure)
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
-- TAGS
-- =========================================================================

INSERT INTO tag (tag_name, note) VALUES 
('AI', N'Artificial Intelligence and Machine Learning'),
('DevOps', N'Software Development, CI/CD, and IT Operations'),
('Academics', N'Academic regulations, coursework, and study guides'),
('CampusLife', N'Student events, campus festivals, and club activities'),
('Scholarship', N'Scholarship announcements and financial support');
GO

-- =========================================================================
-- NEWS ARTICLES
-- =========================================================================

DECLARE @AdminID BIGINT = (SELECT TOP 1 accountid FROM system_account WHERE account_name = 'admin');

-- Article 1: Under Category 1 (Campus News)
INSERT INTO news_article (news_title, headline, news_content, news_source, news_status, created_date, categoryid, created_byid)
VALUES (
    N'University Welcomes New Students for Academic Year 2026 - 2027',
    N'Over 5,000 freshmen officially join the university community',
    N'The opening ceremony was held with great enthusiasm and solemnity, attended by distinguished university leaders, faculty members, and thousands of energetic new students...',
    N'Office of Student Affairs',
    1,
    GETDATE(),
    1,
    @AdminID
);

-- Article 2: Under Category 12 (Artificial Intelligence)
INSERT INTO news_article (news_title, headline, news_content, news_source, news_status, created_date, categoryid, created_byid)
VALUES (
    N'Workshop: Generative AI and Modern Software Engineering Trends',
    N'Industry experts discuss AI agent workflows and digital transformation in 2026',
    N'The workshop featured comprehensive demonstrations on practical machine learning integration, LLM fine-tuning, and automated cloud workflows...',
    N'Faculty of Information Technology',
    1,
    GETDATE(),
    12,
    @AdminID
);

-- Article 3: Under Category 11 (Sports & E-Sports)
INSERT INTO news_article (news_title, headline, news_content, news_source, news_status, created_date, categoryid, created_byid)
VALUES (
    N'Annual Campus Football Championship & E-Sports Tournament Kicks Off',
    N'More than 32 teams compete for the prestigious championship trophy',
    N'The annual sports tournament officially began this weekend with passionate matches, high team spirit, and energetic cheers from thousands of student spectators...',
    N'Campus Sports Committee',
    1,
    GETDATE(),
    11,
    @AdminID
);

-- =========================================================================
-- NEWS TAG MAPPINGS
-- =========================================================================

DECLARE @Article1ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%University Welcomes%');
DECLARE @Article2ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%Workshop: Generative AI%');
DECLARE @Article3ID BIGINT = (SELECT TOP 1 news_articleid FROM news_article WHERE news_title LIKE N'%Annual Campus Football%');

DECLARE @TagCampusLife BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'CampusLife');
DECLARE @TagAI BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'AI');
DECLARE @TagDevOps BIGINT = (SELECT TOP 1 tagid FROM tag WHERE tag_name = 'DevOps');

IF @Article1ID IS NOT NULL AND @TagCampusLife IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article1ID, @TagCampusLife);

IF @Article2ID IS NOT NULL AND @TagAI IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article2ID, @TagAI);

IF @Article2ID IS NOT NULL AND @TagDevOps IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article2ID, @TagDevOps);

IF @Article3ID IS NOT NULL AND @TagCampusLife IS NOT NULL
    INSERT INTO news_tag (news_articleid, tagid) VALUES (@Article3ID, @TagCampusLife);

GO