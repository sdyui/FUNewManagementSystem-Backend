import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router";
import api from "../api/axiosClient";
import { Container, Button, Badge, Spinner, Alert } from "react-bootstrap";

export default function NewsDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [news, setNews] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchDetail = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get(`public/news/${id}`);
        setNews(res.data);
      } catch {
        setError("Bài viết không tồn tại hoặc chưa được xuất bản.");
      } finally {
        setLoading(false);
      }
    };
    void fetchDetail();
  }, [id]);

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" />
      </div>
    );
  }

  if (error || !news) {
    return (
      <Container className="py-4 text-center" style={{ maxWidth: 700 }}>
        <Alert variant="danger">{error || "Không tìm thấy bài viết."}</Alert>
        <Button as={Link} to="/" variant="primary">
          ← Quay về trang chủ
        </Button>
      </Container>
    );
  }

  return (
    <Container className="py-4 text-start" style={{ maxWidth: 820 }}>
      <Button variant="outline-secondary" className="mb-3" onClick={() => void navigate(-1)}>
        ← Quay lại
      </Button>

      {news.categoryName && (
        <Badge bg="primary" className="mb-2 d-block" style={{ width: "fit-content" }}>
          {news.categoryName}
        </Badge>
      )}
      <h1 className="fw-bold">{news.title}</h1>

      <div className="text-muted mb-3">
        {news.authorName && (
          <>
            Tác giả: <strong>{news.authorName}</strong> ·{" "}
          </>
        )}
        {news.publishDate ? new Date(news.publishDate).toLocaleString() : ""}
      </div>

      {news.headline && <h5 className="text-secondary mb-3">{news.headline}</h5>}

      {news.thumbnail && (
        <img
          src={news.thumbnail}
          alt=""
          className="img-fluid rounded my-3 w-100"
          style={{ maxHeight: 420, objectFit: "cover" }}
        />
      )}

      <hr />

      <div style={{ whiteSpace: "pre-line", lineHeight: 1.8, fontSize: "1.1rem" }}>
        {news.content}
      </div>

      {news.newsSource && (
        <div className="mt-4 p-3 bg-light rounded text-muted">
          <strong>Nguồn:</strong> {news.newsSource}
        </div>
      )}
    </Container>
  );
}
