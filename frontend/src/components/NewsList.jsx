import { useState, useEffect } from "react";
import {
  Container,
  Accordion,
  Table,
  Button,
  Form,
  Modal,
  Badge,
  Alert,
  Row,
  Col,
} from "react-bootstrap";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1/";
const ARTICLE_API = `${API_BASE_URL}articles`;
const CATEGORY_API = `${API_BASE_URL}categories`;

export default function NewsList() {
  // State lưu danh sách bài viết hiển thị trên bảng
  const [articles, setArticles] = useState([]);
  // State lưu danh sách category đang active (để chọn Category trong Form)
  const [activeCategories, setActiveCategories] = useState([]);

  // State lọc theo trạng thái (status)
  const [filterStatus, setFilterStatus] = useState("");

  // State cho Modal Thêm / Sửa bài viết
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [title, setTitle] = useState("");
  const [thumbnail, setThumbnail] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState("");

  // State cho Modal Xem chi tiết bài viết (GET /articles/{id})
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedArticle, setSelectedArticle] = useState(null);

  // State hiển thị thông báo thành công / lỗi
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Hàm lấy Header có kèm JWT Token từ localStorage (giống hệt CategoryList)
  const getHeaders = () => {
    let token = localStorage.getItem("accessToken");
    const userStr = localStorage.getItem("user");
    if (!token && userStr) {
      try {
        const userObj = JSON.parse(userStr);
        token = userObj.accessToken || userObj.token;
      } catch {
        // ignore JSON parse error
      }
    }
    const headers = {
      "Content-Type": "application/json",
    };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
  };

  // 1. Lấy danh sách bài viết của tôi từ Backend (GET /api/v1/articles/my)
  const fetchArticles = async () => {
    setError("");
    try {
      let url = `${ARTICLE_API}/my?size=50`;
      if (filterStatus !== "") {
        url += `&status=${encodeURIComponent(filterStatus)}`;
      }
      const response = await fetch(url, {
        headers: getHeaders(),
      });
      const data = await response.json();
      if (response.ok) {
        setArticles(data.content || []);
      } else {
        setError(data.message || "Failed to load articles.");
      }
    } catch {
      setError("Cannot connect to Backend server.");
    }
  };

  // 2. Lấy danh sách Category đang active để làm dropdown chọn Category
  const fetchActiveCategories = async () => {
    try {
      const response = await fetch(`${CATEGORY_API}/active`, {
        headers: getHeaders(),
      });
      if (response.ok) {
        const data = await response.json();
        setActiveCategories(data || []);
      }
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    fetchArticles();
    fetchActiveCategories();
  }, []);

  // 3. Mở Modal Tạo bài viết mới
  const handleOpenCreate = () => {
    setEditingId(null);
    setTitle("");
    setThumbnail("");
    setContent("");
    setCategoryId("");
    setError("");
    setShowModal(true);
  };

  // 4. Mở Modal Chỉnh sửa bài viết
  const handleOpenEdit = (art) => {
    setEditingId(art.id);
    setTitle(art.title || "");
    setThumbnail(art.thumbnail || "");
    setContent(art.content || "");
    setCategoryId(art.categoryId ? String(art.categoryId) : "");
    setError("");
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
    if (!confirmSubmit) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const response = await fetch(`${ARTICLE_API}/${id}/submit`, {
        method: "PUT",
        headers: getHeaders(),
      });
      const data = await response.json();

      if (response.ok) {
        setMessage(data.message || "Submitted article for review successfully!");
        fetchArticles();
      } else {
        setError(data.message || "Cannot submit this article.");
      }
    } catch {
      setError("Error connecting to server.");
    }
  };

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
        <thead>
          <tr>
            <th>#</th>
            <th>ID</th>
            <th>Title</th>
            <th>Category</th>
            <th>Author</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {articles.length > 0 ? (
            articles.map((art, index) => (
              <tr key={art.id}>
                <td>{index + 1}</td>
                <td>{art.id}</td>
                <td>
                  <strong>{art.title}</strong>
                </td>
                <td>{art.categoryName || "Default"}</td>
                <td>{art.author ? art.author.name : "Me"}</td>
                <td>
                  {art.status === "DRAFT" ? (
                    <Badge bg="secondary">DRAFT</Badge>
                  ) : art.status === "PENDING_REVIEW" ? (
                    <Badge bg="warning" text="dark">
                      PENDING_REVIEW
                    </Badge>
                  ) : art.status === "PUBLISHED" ? (
                    <Badge bg="success">PUBLISHED</Badge>
                  ) : (
                    <Badge bg="info">{art.status}</Badge>
                  )}
                </td>
                <td>
                  <Button
                    variant="info"
                    size="sm"
                    className="me-2"
                    onClick={() => handleViewDetail(art.id)}
                  >
                    View
                  </Button>
                  <Button
                    variant="warning"
                    size="sm"
                    className="me-2"
                    disabled={
                      art.status !== "DRAFT" &&
                      art.status !== "CHANGES_REQUESTED"
                    }
                    onClick={() => handleOpenEdit(art)}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="primary"
                    size="sm"
                    disabled={
                      art.status !== "DRAFT" &&
                      art.status !== "CHANGES_REQUESTED"
                    }
                    onClick={() => handleSubmitReview(art.id)}
                  >
                    Submit
                  </Button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7" className="text-center">
                No articles found.
              </td>
            </tr>
          )}
        </tbody>
      </Table>

      {/* Modal Thêm / Sửa Article (cấu trúc y hệt Modal của Category) */}
      <Modal show={showModal} onHide={() => setShowModal(false)} centered>
        <Form onSubmit={handleSave}>
          <Modal.Header closeButton>
            <Modal.Title>
              {editingId === null ? "Add Article" : "Edit Article"}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Title</Form.Label>
              <Form.Control
                type="text"
                required
                placeholder="Enter article title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Category</Form.Label>
              <Form.Select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
              >
                <option value="">-- Default Active Category --</option>
                {activeCategories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.categoryName}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Thumbnail URL</Form.Label>
              <Form.Control
                type="text"
                placeholder="Enter thumbnail image URL"
                value={thumbnail}
                onChange={(e) => setThumbnail(e.target.value)}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Content</Form.Label>
              <Form.Control
                as="textarea"
                rows={4}
                placeholder="Enter article content"
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowModal(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit">
              Save
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      {/* Modal Xem chi tiết Article (GET /api/v1/articles/{id}) */}
      <Modal
        show={showDetailModal}
        onHide={() => setShowDetailModal(false)}
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Article Detail</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedArticle && (
            <div>
              <p>
                <strong>ID:</strong> {selectedArticle.id}
              </p>
              <p>
                <strong>Title:</strong> {selectedArticle.title}
              </p>
              <p>
                <strong>Status:</strong> {selectedArticle.status}
              </p>
              <p>
                <strong>Category:</strong>{" "}
                {selectedArticle.categoryName || "Default"}
              </p>
              <p>
                <strong>Author:</strong>{" "}
                {selectedArticle.author ? selectedArticle.author.name : "N/A"}
              </p>
              <p>
                <strong>Thumbnail:</strong> {selectedArticle.thumbnail || "N/A"}
              </p>
              <p>
                <strong>Content:</strong>
              </p>
              <div className="p-2 bg-light border rounded">
                {selectedArticle.content || "(No content)"}
              </div>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowDetailModal(false)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}
