-- Sử dụng database
USE SBA_ASSIGNMENT;
GO

-- 1. Chèn tài khoản Admin (chỉ chèn nếu chưa tồn tại, tránh lỗi trùng lặp UNIQUE KEY)
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
-- DỮ LIỆU TEST DEMO CHO PHẦN CATEGORY (QUẢN LÝ CHUYÊN MỤC)
-- =========================================================================

-- Xóa dữ liệu cũ nếu muốn reset lại từ đầu
DELETE FROM category;
GO

-- Bật tính năng chèn ID trực tiếp để cố định mối quan hệ Cha - Con chính xác 100%
SET IDENTITY_INSERT category ON;
GO

-- 2. Chèn toàn bộ danh mục Cha và Con
INSERT INTO category (categoryid, category_name, category_description, is_active, parent_categoryid) VALUES
-- Danh mục cha gốc (IDs 1 -> 5, parent_categoryid = NULL)
(1, N'Tin tức học đường', N'Các tin tức, thông báo và sự kiện diễn ra tại trường đại học', 1, NULL),
(2, N'Đào tạo & Tuyển sinh', N'Thông tin chương trình đào tạo, quy chế học vụ và tuyển sinh các hệ', 1, NULL),
(3, N'Hoạt động sinh viên', N'Các phong trào đoàn hội, hoạt động ngoại khóa và câu lạc bộ sinh viên', 1, NULL),
(4, N'Công nghệ & Sáng tạo', N'Tin tức về công nghệ, lập trình, trí tuệ nhân tạo và nghiên cứu khoa học', 1, NULL),
(5, N'Lưu trữ cũ (Tạm ngưng)', N'Danh mục cũ tạm thời không còn hoạt động (dùng để test lọc Inactive)', 0, NULL),

-- Danh mục con thuộc ID = 1 (Tin tức học đường)
(6, N'Lịch thi & Đăng ký môn', N'Thông báo về lịch thi học kỳ và thời gian mở đăng ký tín chỉ', 1, 1),
(7, N'Học bổng & Khen thưởng', N'Thông tin các suất học bổng khuyến học, tài trợ và khen thưởng sinh viên giỏi', 1, 1),

-- Danh mục con thuộc ID = 2 (Đào tạo & Tuyển sinh)
(8, N'Tuyển sinh Đại học', N'Thông tin đề án tuyển sinh, các phương thức xét tuyển và chỉ tiêu tuyển sinh', 1, 2),
(9, N'Trao đổi & Liên kết quốc tế', N'Các chương trình du học chuyển tiếp và học kỳ trao đổi sinh viên nước ngoài', 1, 2),

-- Danh mục con thuộc ID = 3 (Hoạt động sinh viên)
(10, N'Câu lạc bộ & Đội nhóm', N'Thông tin tuyển thành viên và hoạt động của các CLB học thuật, tình nguyện', 1, 3),
(11, N'Giải đấu Thể thao & E-Sports', N'Các giải bóng đá, bóng bàn, cờ vua và giải đấu thể thao điện tử học đường', 1, 3),

-- Danh mục con thuộc ID = 4 (Công nghệ & Sáng tạo)
(12, N'Trí tuệ nhân tạo (AI)', N'Nghiên cứu ứng dụng Machine Learning, Generative AI và xử lý ngôn ngữ tự nhiên', 1, 4),
(13, N'Kỹ thuật Phần mềm', N'Xu hướng kiến trúc phần mềm, phát triển ứng dụng Web, Cloud và DevOps', 1, 4);
GO

-- Tắt tính năng chèn ID trực tiếp để DB tự tăng ID cho các lần tạo mới sau này từ API/Front-end
SET IDENTITY_INSERT category OFF;
GO