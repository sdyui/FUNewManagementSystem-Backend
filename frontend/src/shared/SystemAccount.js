// systemAccounts.js

export const systemAccounts = [
  {
    AccountID: 1,
    AccountName: "Administrator",
    AccountEmail: "admin@newsportal.com",
    AccountRole: "Administrator", // Quyền quản trị viên cao nhất
    AccountPassword: "hashed_password_1",
  },
  {
    AccountID: 2,
    AccountName: "Editor Staff",
    AccountEmail: "editor@newsportal.com",
    AccountRole: "Editor", // Quyền biên tập viên duyệt bài
    AccountPassword: "hashed_password_2",
  },
  {
    AccountID: 3,
    AccountName: "John Reporter",
    AccountEmail: "john@newsportal.com",
    AccountRole: "Staff", // Quyền phóng viên/nhân viên tạo bài viết
    AccountPassword: "hashed_password_3",
  },
  {
    AccountID: 4,
    AccountName: "Sarah Connor",
    AccountEmail: "sarah@newsportal.com",
    AccountRole: "Staff",
    AccountPassword: "hashed_password_4",
  },
];
