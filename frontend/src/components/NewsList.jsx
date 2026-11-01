import { useState } from "react";
import { Container, Table, Button, Form, InputGroup } from "react-bootstrap";
import { Link, useSearchParams } from "react-router";

import { News } from "../shared/News";
import NewsModal from "../pages/NewsForm";

const newsArticles = Array.isArray(News) ? News : [News];

// Category ID -> Category Name
const categories = {
  1: "Technology",
  2: "Artificial Intelligence",
  3: "Business",
};

export default function NewsList() {
  const [searchTerm, setSearchTerm] = useState("");

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState("add");
  const [selectedNews, setSelectedNews] = useState(null);


  const handleAdd = () => {
    setModalMode("add");
    setSelectedNews(null);
    setShowModal(true);
  };

  const handleEdit = (news) => {
    setModalMode("edit");
    setSelectedNews(news);
    setShowModal(true);
  };

  // Lọc theo category
  let newsList = CategoryID
    ? newsArticles.filter((item) => String(item.CategoryID) === CategoryID)
    : newsArticles;

  // Lọc theo từ khóa
  if (searchTerm.trim() !== "") {
    const keyword = searchTerm.toLowerCase();

    newsList = newsList.filter(
      (item) =>
        item.NewsTitle?.toLowerCase().includes(keyword) ||
        item.Headline?.toLowerCase().includes(keyword) ||
        item.NewsSource?.toLowerCase().includes(keyword) ||
        categories[item.CategoryID]?.toLowerCase().includes(keyword),
    );
  }

  return (
    <Container className="mt-4">
      {/* Header */}
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <h2 className="mb-0">
          {CategoryID
            ? `Tin tức thuộc danh mục: ${categories[categoryId] || categoryId}`
            : "Tất cả bài viết"}
        </h2>

        {/* Search */}
        <InputGroup style={{ maxWidth: "400px" }}>
          <Form.Control
            placeholder="Tìm kiếm tin tức..."
            aria-label="Tìm kiếm tin tức"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />

          <Button variant="primary">Search</Button>
        </InputGroup>

        {/* Add */}
        <Button variant="success" className="text-nowrap" onClick={handleAdd}>
          + Add News
        </Button>
      </div>

      {/* News Table */}
      <Table striped bordered hover responsive>
        <thead className="table-light">
          <tr>
            <th>#</th>
            <th>News Title</th>
            <th>Headline</th>
            <th>Source</th>
            <th>Created Date</th>
            <th>Category</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {newsList.length > 0 ? (
            newsList.map((item, index) => (
              <tr key={item.NewsArticleID}>
                <td>{index + 1}</td>

                <td>
                  <strong>{item.NewsTitle}</strong>
                </td>

                <td>{item.Headline}</td>

                <td>{item.NewsSource}</td>

                <td>
                  {item.CreatedDate
                    ? new Date(item.CreatedDate).toLocaleDateString()
                    : ""}
                </td>

                {/* Category */}
                <td>{categories[item.CategoryID] || "Unknown"}</td>

                {/* Action */}
                <td>
                  <div className="d-flex gap-2">
                    {/* Details */}
                    <Button
                      as={Link}
                      to={`/news/${item.NewsArticleID}`}
                      variant="primary"
                      size="sm"
                      className="flex-fill"
                    >
                      Details
                    </Button>

                    {/* Edit */}
                    <Button
                      variant="warning"
                      size="sm"
                      className="flex-fill"
                      onClick={() => handleEdit(item)}
                    >
                      Edit
                    </Button>

                    {/* Delete */}
                    <Button
                      variant="danger"
                      size="sm"
                      className="flex-fill"
                      onClick={() => {
                        const cf = window.confirm(
                          "Bạn có chắc muốn xóa bài viết này không?"
                        );
                      }}
                    >
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7" className="text-center">
                Không tìm thấy bài viết nào.
              </td>
            </tr>
          )}
        </tbody>
      </Table>

      <NewsModal
        show={showModal}
        onHide={() => setShowModal(false)}
        mode={modalMode}
        news={selectedNews}
      />
    </Container>
  );
}
