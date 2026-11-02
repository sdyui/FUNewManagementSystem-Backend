import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router";
import {
  Container,
  Card,
  Button,
  Badge,
  Alert,
  Row,
  Col,
} from "react-bootstrap";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1/";
const ARTICLE_API = `${API_BASE_URL}articles`;

export default function NewsDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [article, setArticle] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const getHeaders = () => {
    let token = localStorage.getItem("accessToken");
    const userStr = localStorage.getItem("user");
    if (!token && userStr) {
      try {
        const userObj = JSON.parse(userStr);
        token = userObj.accessToken || userObj.token;
      } catch {
        // ignore
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

  // Lấy chi tiết bài viết theo ID (GET /api/v1/articles/{id})
  const fetchArticleDetail = async () => {
    setError("");
    try {
      const response = await fetch(`${ARTICLE_API}/${id}`, {
        headers: getHeaders(),
      });
      const data = await response.json();
      if (response.ok) {
        setArticle(data);
      } else {
        setError(data.message || "Không tìm thấy bài viết.");
      }
    } catch {
      setError("Không thể kết nối tới máy chủ Backend.");
    }
  };

  useEffect(() => {
    fetchArticleDetail();
  }, [id]);

  // Gửi bài viết để xét duyệt ngay từ trang chi tiết (PUT /api/v1/articles/{id}/submit)
  const handleSubmitForReview = async () => {
    const confirmSubmit = window.confirm(
      "Bạn có chắc chắn muốn gửi bài viết này để xét duyệt?"
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
        setMessage(data.message || "Đã gửi xét duyệt thành công!");
        fetchArticleDetail();
      } else {
        setError(data.message || "Không thể gửi xét duyệt.");
      }
    } catch {
      setError("Lỗi kết nối tới máy chủ Backend.");
    }
  };

  const renderStatusBadge = (status) => {
    if (status === "DRAFT") {
      return <Badge bg="secondary">DRAFT</Badge>;
    }
    if (status === "PENDING_REVIEW") {
      return <Badge bg="warning" text="dark">PENDING_REVIEW</Badge>;
    }
    if (status === "CHANGES_REQUESTED") {
      return <Badge bg="danger">CHANGES_REQUESTED</Badge>;
    }
    if (status === "APPROVED") {
      return <Badge bg="info">APPROVED</Badge>;
    }
    if (status === "PUBLISHED") {
      return <Badge bg="success">PUBLISHED</Badge>;
    }
    if (status === "REJECTED") {
      return <Badge bg="dark">REJECTED</Badge>;
    }
    return <Badge bg="secondary">{status}</Badge>;
  };

  const canSubmit =
    article &&
    (article.status === "DRAFT" || article.status === "CHANGES_REQUESTED");

  return (
    <Container className="mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <Button variant="secondary" onClick={() => navigate("/news")}>
          ← Back to Articles
        </Button>

        {canSubmit && (
          <Button variant="primary" onClick={handleSubmitForReview}>
            Submit for Review
          </Button>
        )}
      </div>

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

      {article && (
        <Card>
          <Card.Header className="d-flex justify-content-between align-items-center">
            <span>Article ID: #{article.id}</span>
            {renderStatusBadge(article.status)}
          </Card.Header>
          <Card.Body>
            <Card.Title className="fs-3 mb-3">{article.title}</Card.Title>

            <Row className="mb-3 text-muted small">
              <Col md={4}>
                <strong>Author:</strong>{" "}
                {article.author
                  ? `${article.author.name} (${article.author.email})`
                  : "N/A"}
              </Col>
              <Col md={4}>
                <strong>Category:</strong> {article.categoryName || "N/A"}
              </Col>
              <Col md={4}>
                <strong>Created At:</strong>{" "}
                {article.created_at
                  ? new Date(article.created_at).toLocaleString()
                  : "N/A"}
              </Col>
            </Row>

            {article.tagNames && article.tagNames.length > 0 && (
              <div className="mb-3">
                <strong>Tags: </strong>
                {article.tagNames.map((t, idx) => (
                  <Badge bg="info" className="me-1" key={idx}>
                    {t}
                  </Badge>
                ))}
              </div>
            )}

            {article.thumbnail && (
              <div className="mb-3">
                <strong>Thumbnail: </strong>
                <a href={article.thumbnail} target="_blank" rel="noreferrer">
                  {article.thumbnail}
                </a>
              </div>
            )}

            <hr />

            <Card.Text style={{ whiteSpace: "pre-line" }}>
              {article.content || "(Chưa có nội dung)"}
            </Card.Text>
          </Card.Body>
          {article.updated_at && (
            <Card.Footer className="text-muted small">
              Last updated: {new Date(article.updated_at).toLocaleString()}
            </Card.Footer>
          )}
        </Card>
      )}
    </Container>
  );
}
