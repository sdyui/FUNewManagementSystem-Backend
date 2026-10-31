/* =====================================================================
   DU LIEU TEST - Editorial Workflow (Review & Publish)
   DB: SBA_ASSIGNMENT (SQL Server)

   Cach chay: mo file trong SSMS / Azure Data Studio -> Execute (F5)
   Chay lai nhieu lan duoc: script tu xoa du lieu test cu truoc khi them moi.
   Khong dung den tai khoan "admin" va du lieu khac cua ban.

   Tai khoan test (mat khau giong tai khoan admin, dung chung hash BCrypt):
     editor01  - EDITOR  (duyet bai)
     staff01   - STAFF   (tac gia)
     staff02   - STAFF   (tac gia)
   ===================================================================== */

USE SBA_ASSIGNMENT;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

/* ---------- 0. Sua schema cho khop entity hien tai ---------- */

-- Cot thumbnail (entity co @Column(name="ThumbNail") nhung DB chua co)
IF COL_LENGTH('news_article', 'thumb_nail') IS NULL
    ALTER TABLE news_article ADD thumb_nail VARCHAR(500) NULL;

-- CHECK constraint cua account_role chua co CONTRIBUTOR -> tao lai cho du 4 role
DECLARE @ck SYSNAME = (SELECT TOP 1 name FROM sys.check_constraints
                       WHERE parent_object_id = OBJECT_ID('system_account')
                         AND definition LIKE '%account_role%');
IF @ck IS NOT NULL
    EXEC('ALTER TABLE system_account DROP CONSTRAINT ' + @ck);
ALTER TABLE system_account ADD CONSTRAINT CK_system_account_role
    CHECK (account_role IN ('ADMIN', 'STAFF', 'EDITOR', 'CONTRIBUTOR'));
GO

BEGIN TRANSACTION;

/* ---------- 1. Xoa du lieu test cu ---------- */
DECLARE @testAccounts TABLE (id BIGINT);
INSERT INTO @testAccounts
SELECT accountid FROM system_account WHERE account_name IN ('editor01', 'staff01', 'staff02');

DECLARE @testArticles TABLE (id BIGINT);
INSERT INTO @testArticles
SELECT news_articleid FROM news_article WHERE created_byid IN (SELECT id FROM @testAccounts);

DELETE FROM article_workflow_log WHERE news_articleid IN (SELECT id FROM @testArticles);
DELETE FROM article_workflow_log WHERE actorid IN (SELECT id FROM @testAccounts);
DELETE FROM news_tag             WHERE news_articleid IN (SELECT id FROM @testArticles);
UPDATE news_article SET updated_byid = NULL WHERE updated_byid IN (SELECT id FROM @testAccounts);
DELETE FROM news_article         WHERE news_articleid IN (SELECT id FROM @testArticles);
DELETE FROM system_account       WHERE accountid IN (SELECT id FROM @testAccounts);

/* ---------- 2. Tai khoan ---------- */
DECLARE @pw VARCHAR(255) = (SELECT account_password FROM system_account WHERE account_name = 'admin');
IF @pw IS NULL
    THROW 50001, 'Khong tim thay tai khoan admin de lay hash mat khau', 1;

INSERT INTO system_account (account_name, account_email, account_password, account_role) VALUES
    ('editor01', 'editor01@fu.edu.vn', @pw, 'EDITOR'),
    ('staff01',  'staff01@fu.edu.vn',  @pw, 'STAFF'),
    ('staff02',  'staff02@fu.edu.vn',  @pw, 'STAFF');

DECLARE @editor BIGINT = (SELECT accountid FROM system_account WHERE account_name = 'editor01');
DECLARE @staff1 BIGINT = (SELECT accountid FROM system_account WHERE account_name = 'staff01');
DECLARE @staff2 BIGINT = (SELECT accountid FROM system_account WHERE account_name = 'staff02');

/* ---------- 3. Category & Tag (chi them neu chua co) ---------- */
IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Academic')
    INSERT INTO category (category_name, category_description, is_active) VALUES ('Academic', 'Tin hoc thuat', 1);
IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Student Life')
    INSERT INTO category (category_name, category_description, is_active) VALUES ('Student Life', 'Doi song sinh vien', 1);
IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Events')
    INSERT INTO category (category_name, category_description, is_active) VALUES ('Events', 'Su kien', 1);
IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Old News')
    INSERT INTO category (category_name, category_description, is_active) VALUES ('Old News', 'Danh muc da ngung', 0);

DECLARE @catAcademic BIGINT = (SELECT TOP 1 categoryid FROM category WHERE category_name = 'Academic');
DECLARE @catLife     BIGINT = (SELECT TOP 1 categoryid FROM category WHERE category_name = 'Student Life');
DECLARE @catEvents   BIGINT = (SELECT TOP 1 categoryid FROM category WHERE category_name = 'Events');

IF NOT EXISTS (SELECT 1 FROM tag WHERE tag_name = 'fpt')        INSERT INTO tag (tag_name, note) VALUES ('fpt', 'FPT University');
IF NOT EXISTS (SELECT 1 FROM tag WHERE tag_name = 'hackathon')  INSERT INTO tag (tag_name, note) VALUES ('hackathon', NULL);
IF NOT EXISTS (SELECT 1 FROM tag WHERE tag_name = 'scholarship') INSERT INTO tag (tag_name, note) VALUES ('scholarship', NULL);

DECLARE @tagFpt  BIGINT = (SELECT tagid FROM tag WHERE tag_name = 'fpt');
DECLARE @tagHack BIGINT = (SELECT tagid FROM tag WHERE tag_name = 'hackathon');
DECLARE @tagSch  BIGINT = (SELECT tagid FROM tag WHERE tag_name = 'scholarship');

/* ---------- 4. Bai viet: moi trang thai it nhat 1 bai ---------- */
DECLARE @now DATETIME2 = SYSDATETIME();
DECLARE @a1 BIGINT, @a2 BIGINT, @a3 BIGINT, @a4 BIGINT, @a5 BIGINT,
        @a6 BIGINT, @a7 BIGINT, @a8 BIGINT, @a9 BIGINT;

-- A1: DRAFT cua staff01 -> editor xem phai 404
INSERT INTO news_article (news_title, headline, news_content, news_source, thumb_nail, categoryid,
                          news_status, created_byid, created_date, article_status, version)
VALUES ('[TEST] A1 Draft staff01', 'Ban nhap', N'Nội dung bản nháp của staff01', NULL, NULL, @catAcademic,
        0, @staff1, DATEADD(DAY, -1, @now), 'DRAFT', 0);
SET @a1 = SCOPE_IDENTITY();

-- A2: DRAFT cua staff02
INSERT INTO news_article (news_title, headline, news_content, categoryid,
                          news_status, created_byid, created_date, article_status, version)
VALUES ('[TEST] A2 Draft staff02', 'Ban nhap', N'Nội dung bản nháp của staff02', @catLife,
        0, @staff2, DATEADD(HOUR, -5, @now), 'DRAFT', 0);
SET @a2 = SCOPE_IDENTITY();

-- A3: PENDING_REVIEW, gui luc 3 ngay truoc -> dung DAU hang doi
INSERT INTO news_article (news_title, headline, news_content, categoryid,
                          news_status, created_byid, created_date, modified_date, updated_byid, article_status, version)
VALUES ('[TEST] A3 Pending (old)', 'Cho duyet', N'Bài chờ duyệt gửi sớm nhất', @catAcademic,
        0, @staff1, DATEADD(DAY, -4, @now), DATEADD(DAY, -3, @now), @staff1, 'PENDING_REVIEW', 0);
SET @a3 = SCOPE_IDENTITY();

-- A4: PENDING_REVIEW, gui 1 gio truoc -> dung SAU A3
INSERT INTO news_article (news_title, headline, news_content, categoryid,
                          news_status, created_byid, created_date, modified_date, updated_byid, article_status, version)
VALUES ('[TEST] A4 Pending (new)', 'Cho duyet', N'Bài chờ duyệt gửi gần đây', @catEvents,
        0, @staff2, DATEADD(DAY, -1, @now), DATEADD(HOUR, -1, @now), @staff2, 'PENDING_REVIEW', 0);
SET @a4 = SCOPE_IDENTITY();

-- A5: CHANGES_REQUESTED -> tac gia thay latest_feedback
INSERT INTO news_article (news_title, headline, news_content, categoryid,
                          news_status, created_byid, created_date, modified_date, updated_byid, article_status, version)
VALUES ('[TEST] A5 Changes requested', 'Can sua', N'Bài bị editor trả về', @catLife,
        0, @staff1, DATEADD(DAY, -3, @now), DATEADD(DAY, -2, @now), @editor, 'CHANGES_REQUESTED', 0);
SET @a5 = SCOPE_IDENTITY();

-- A6: SCHEDULED, hen dang sau 3 phut -> test scheduler tu dang
INSERT INTO news_article (news_title, headline, news_content, categoryid,
                          news_status, created_byid, created_date, modified_date, updated_byid, article_status, publish_date, version)
VALUES ('[TEST] A6 Scheduled (+3 min)', 'Len lich', N'Bài hẹn giờ đăng sau 3 phút', @catEvents,
        0, @staff2, DATEADD(DAY, -2, @now), DATEADD(HOUR, -2, @now), @editor, 'SCHEDULED', DATEADD(MINUTE, 3, @now), 0);
SET @a6 = SCOPE_IDENTITY();

-- A7: SCHEDULED da qua gio -> scheduler dang ngay lan chay dau tien
INSERT INTO news_article (news_title, headline, news_content, categoryid,
                          news_status, created_byid, created_date, modified_date, updated_byid, article_status, publish_date, version)
VALUES ('[TEST] A7 Scheduled (overdue)', 'Len lich', N'Bài hẹn giờ đã quá hạn', @catAcademic,
        0, @staff1, DATEADD(DAY, -2, @now), DATEADD(DAY, -1, @now), @editor, 'SCHEDULED', DATEADD(MINUTE, -5, @now), 0);
SET @a7 = SCOPE_IDENTITY();

-- A8: PUBLISHED -> test archive (go bai)
INSERT INTO news_article (news_title, headline, news_content, news_source, categoryid,
                          news_status, created_byid, created_date, modified_date, updated_byid, article_status, publish_date, version)
VALUES ('[TEST] A8 Published', 'Da dang', N'Bài đã đăng công khai', 'FPT News', @catEvents,
        1, @staff2, DATEADD(DAY, -6, @now), DATEADD(DAY, -5, @now), @editor, 'PUBLISHED', DATEADD(DAY, -5, @now), 0);
SET @a8 = SCOPE_IDENTITY();

-- A9: ARCHIVED (bi tu choi co ghi chu)
INSERT INTO news_article (news_title, headline, news_content, categoryid,
                          news_status, created_byid, created_date, modified_date, updated_byid, article_status, version)
VALUES ('[TEST] A9 Rejected', 'Bi tu choi', N'Bài bị từ chối', @catLife,
        0, @staff1, DATEADD(DAY, -7, @now), DATEADD(DAY, -6, @now), @editor, 'ARCHIVED', 0);
SET @a9 = SCOPE_IDENTITY();

/* ---------- 5. Tag cho bai ---------- */
INSERT INTO news_tag (news_articleid, tagid) VALUES
    (@a3, @tagFpt), (@a3, @tagSch),
    (@a4, @tagHack),
    (@a6, @tagFpt), (@a6, @tagHack),
    (@a8, @tagFpt);

/* ---------- 6. Audit log khop voi trang thai hien tai ----------
   Luu y: cot from_status dang NOT NULL nen khong co dong "tao bai" (from = NULL). */
INSERT INTO article_workflow_log (news_articleid, from_status, to_status, actorid, note, action_at) VALUES
    -- A3, A4: gui duyet
    (@a3, 'DRAFT', 'PENDING_REVIEW', @staff1, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -3, @now)),
    (@a4, 'DRAFT', 'PENDING_REVIEW', @staff2, N'Tác giả gửi bài viết để xét duyệt', DATEADD(HOUR, -1, @now)),

    -- A5: gui -> bi tra ve
    (@a5, 'DRAFT', 'PENDING_REVIEW', @staff1, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -3, @now)),
    (@a5, 'PENDING_REVIEW', 'CHANGES_REQUESTED', @editor, N'Thiếu nguồn tham khảo, bổ sung ảnh minh họa', DATEADD(DAY, -2, @now)),

    -- A6: gui -> duyet hen gio
    (@a6, 'DRAFT', 'PENDING_REVIEW', @staff2, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -1, @now)),
    (@a6, 'PENDING_REVIEW', 'SCHEDULED', @editor, N'Duyệt, đăng theo lịch', DATEADD(HOUR, -2, @now)),

    -- A7: gui -> duyet hen gio
    (@a7, 'DRAFT', 'PENDING_REVIEW', @staff1, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -2, @now)),
    (@a7, 'PENDING_REVIEW', 'SCHEDULED', @editor, NULL, DATEADD(DAY, -1, @now)),

    -- A8: gui -> bi tra ve -> gui lai -> dang ngay
    (@a8, 'DRAFT', 'PENDING_REVIEW', @staff2, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -6, @now)),
    (@a8, 'PENDING_REVIEW', 'CHANGES_REQUESTED', @editor, N'Sửa lại tiêu đề', DATEADD(HOUR, -140, @now)),
    (@a8, 'CHANGES_REQUESTED', 'PENDING_REVIEW', @staff2, N'Tác giả gửi bài viết để xét duyệt', DATEADD(HOUR, -130, @now)),
    (@a8, 'PENDING_REVIEW', 'PUBLISHED', @editor, NULL, DATEADD(DAY, -5, @now)),

    -- A9: gui -> tu choi
    (@a9, 'DRAFT', 'PENDING_REVIEW', @staff1, N'Tác giả gửi bài viết để xét duyệt', DATEADD(DAY, -7, @now)),
    (@a9, 'PENDING_REVIEW', 'ARCHIVED', @editor, N'Nội dung trùng với bài đã đăng', DATEADD(DAY, -6, @now));

COMMIT TRANSACTION;
GO

/* ---------- 7. Xem nhanh ket qua ---------- */
SELECT a.news_articleid AS id, a.news_title, a.article_status, a.news_status,
       u.account_name AS author, a.modified_date, a.publish_date
FROM news_article a
JOIN system_account u ON u.accountid = a.created_byid
WHERE a.news_title LIKE '[[]TEST]%'
ORDER BY a.news_articleid;
