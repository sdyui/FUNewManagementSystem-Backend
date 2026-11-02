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

  // 5. Lưu bài viết (Tạo mới POST hoặc Cập nhật PUT)
  const handleSave = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    const requestBody = {
      title: title,
      thumbnail: thumbnail,
      content: content,
      categoryId: categoryId === "" ? null : Number(categoryId),
    };

    try {
      let response;
      if (editingId === null) {
        response = await fetch(ARTICLE_API, {
          method: "POST",
          headers: getHeaders(),
          body: JSON.stringify(requestBody),
        });
      } else {
        response = await fetch(`${ARTICLE_API}/${editingId}`, {
          method: "PUT",
          headers: getHeaders(),
          body: JSON.stringify(requestBody),
        });
      }

      const resData = await response.json();

      if (response.ok) {
        if (editingId === null) {
          setMessage("Created article (DRAFT) successfully!");
        } else {
          setMessage("Updated article successfully!");
        }
        setShowModal(false);
        fetchArticles();
      } else {
        setError(resData.message || "Failed to save article.");
      }
    } catch {
      setError("Error connecting to server.");
    }
  };

  // 6. Xem chi tiết bài viết (GET /api/v1/articles/{id})
  const handleViewDetail = async (id) => {
    setError("");
    try {
      const response = await fetch(`${ARTICLE_API}/${id}`, {
        headers: getHeaders(),
      });
      const data = await response.json();
      if (response.ok) {
        setSelectedArticle(data);
        setShowDetailModal(true);
      } else {
        setError(data.message || "Failed to load article details.");
      }
    } catch {
      setError("Error connecting to server.");
    }
  };

  // 7. Gửi bài viết để xét duyệt (PUT /api/v1/articles/{id}/submit)
  const handleSubmitReview = async (id) => {
    const confirmSubmit = window.confirm(
      "Are you sure you want to submit this article for review?"
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
      <h2 className="mb-3">Article Management</h2>

      {message && (
        <Alert variant="success" onClose={() => setMessage("")} dismissible>
          {message}
        </Alert>
      )}
      {error && (
        <Alert variant="danger" onClose={() => setError("")} dismissible>
          {error}
        </Alert>
      )}

      {/* Thanh công cụ Lọc theo Status & Thêm mới dùng Accordion giống hệt Category */}
      <Accordion defaultActiveKey="0" className="mb-4">
        <Accordion.Item eventKey="0">
          <Accordion.Header>Filter & Actions</Accordion.Header>
          <Accordion.Body>
            <Form
              onSubmit={(e) => {
                e.preventDefault();
                fetchArticles();
              }}
            >
              <Row className="align-items-center g-2">
                <Col md={8}>
                  <Form.Select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                  >
                    <option value="">-- All Status --</option>
                    <option value="DRAFT">DRAFT</option>
                    <option value="PENDING_REVIEW">PENDING_REVIEW</option>
                    <option value="CHANGES_REQUESTED">CHANGES_REQUESTED</option>
                    <option value="APPROVED">APPROVED</option>
                    <option value="PUBLISHED">PUBLISHED</option>
                    <option value="REJECTED">REJECTED</option>
                  </Form.Select>
                </Col>
                <Col md={4} className="d-flex gap-2">
                  <Button variant="primary" type="submit">
                    Filter
                  </Button>
                  <Button variant="success" onClick={handleOpenCreate}>
                    + Add Article
                  </Button>
                </Col>
              </Row>
            </Form>
          </Accordion.Body>
        </Accordion.Item>
      </Accordion>

      {/* Bảng danh sách Article */}
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
