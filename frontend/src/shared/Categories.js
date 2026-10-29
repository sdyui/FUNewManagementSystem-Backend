// categories.js

export const categories = [
  {
    CategoryID: 1,
    CategoryName: "Technology",
    CategoryDesciption:
      "Latest news about technology, software development, and hardware innovations.",
    ParentCategoryID: null, // Danh mục gốc (Cha)
    IsActive: true,
  },
  {
    CategoryID: 2,
    CategoryName: "Artificial Intelligence",
    CategoryDesciption:
      "Subcategory focusing on AI, Machine Learning, and Neural Networks.",
    ParentCategoryID: 1, // Thuộc danh mục con của Technology (CategoryID: 1)
    IsActive: true,
  },
  {
    CategoryID: 3,
    CategoryName: "Business",
    CategoryDesciption:
      "Global business updates, stock markets, and startup entrepreneurship.",
    ParentCategoryID: null, // Danh mục gốc (Cha)
    IsActive: true,
  },
  {
    CategoryID: 4,
    CategoryName: "Fintech",
    CategoryDesciption:
      "Financial technology, digital banking, and blockchain news.",
    ParentCategoryID: 3, // Thuộc danh mục con của Business (CategoryID: 3)
    IsActive: true,
  },
  {
    CategoryID: 5,
    CategoryName: "Cybersecurity",
    CategoryDesciption:
      "Information security, data privacy, and threat intelligence.",
    ParentCategoryID: 1, // Thuộc danh mục con của Technology (CategoryID: 1)
    IsActive: true,
  },
];
