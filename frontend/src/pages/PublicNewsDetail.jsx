import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { Container, Button, Badge, Spinner } from "react-bootstrap";
import api from "../api/axiosClient";
import NotFound from "./NotFound";

export default function PublicNewsDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  // Kết quả của lần tải gần nhất, kèm id của bài đã tải
  const [result, setResult] = useState({ id: null, news: null, notFound: false });

  useEffect(() => {
    api
      .get(`public/news/${id}`)
      .then((res) => setResult({ id, news: res.data, notFound: false }))
      .catch(() => setResult({ id, news: null, notFound: true })); // 404: bài chưa đăng / không tồn tại
  }, [id]);

  // Đang tải khi kết quả hiện có chưa phải của bài đang mở
  const status = result.id !== id ? "loading" : result.notFound ? "notfound" : "ok";
  const news = result.news;

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
    <Container className="py-2 text-start" style={{ maxWidth: 820 }}>
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

      {/* Text thường: React tự escape, tránh XSS */}
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
