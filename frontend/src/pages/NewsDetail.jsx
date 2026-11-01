import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import api from "../api/axiosClient";
import { Container, Button, Badge, Spinner } from "react-bootstrap";
import NotFound from "./NotFound";

export default function NewsDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [news, setNews] = useState(null);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    setStatus("loading");
    api
      .get(`public/news/${id}`)
      .then((res) => {
        setNews(res.data);
        setStatus("ok");
      })
      .catch(() => setStatus("notfound"));
  }, [id]);
  if (status === "loading") {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" />
      </div>
    );
  }
  if (status === "notfound") {
    return <NotFound />;
  }
  return (
    <Container className="py-4 text-start" style={{ maxWidth: 820 }}>
      <Button variant="link" className="px-0 mb-3" onClick={() => navigate(-1)}>
        ← Back
      </Button>

      {news.categoryName && (
        <Badge bg="primary" className="mb-2">
          {news.categoryName}
        </Badge>
      )}
      <h1 className="fw-bold">{news.title}</h1>

      <div className="text-muted mb-3">
        {news.authorName && (
          <>
            By <strong>{news.authorName}</strong> ·{" "}
          </>
        )}
        {news.publishDate ? new Date(news.publishDate).toLocaleString() : ""}
      </div>

      {news.headline && <h5 className="text-secondary">{news.headline}</h5>}

      {news.thumbnail && (
        <img src={news.thumbnail} alt="" className="img-fluid rounded my-3" />
      )}

      <hr />

      {/* Hiển thị dạng text thường: React tự escape, tránh XSS */}
      <div style={{ whiteSpace: "pre-line", lineHeight: 1.8 }}>
        {news.content}
      </div>

      {news.newsSource && (
        <div className="mt-4 p-3 bg-light rounded">
          <strong>Source:</strong> {news.newsSource}
        </div>
      )}
    </Container>
  );
}
