import { useNavigate, useParams } from "react-router";
import { Container, Row, Col, Button, Badge } from "react-bootstrap";
import { News } from "../shared/News";
import NotFound from "./NotFound";

export default function NewsDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const newsArticles = Array.isArray(News) ? News : [News];

  const news = newsArticles.find(
    (item) => String(item.NewsArticleID) === String(id),
  );

  if (!news) {
    return <NotFound />;
  }

  return (
    <Container className="mt-4 text-start">
      {/* Back */}
      <Button variant="link" className="px-0 mb-3" onClick={() => navigate(-1)}>
        ← Back
      </Button>

      <Row className="g-4">
        {/* News content */}
        <Col md={12}>
          <h1 className="fw-bold">{news.NewsTitle}</h1>

          <div className="mb-3">
            <Badge bg="secondary" className="me-2">
              Category: {news.CategoryID}
            </Badge>

            <span className="text-muted">
              {news.CreatedDate
                ? new Date(news.CreatedDate).toLocaleDateString()
                : ""}
            </span>
          </div>

          {/* Headline */}
          <h5 className="text-secondary">{news.Headline}</h5>

          <hr />

          {/* Content */}
          <p
            style={{
              whiteSpace: "pre-line",
              lineHeight: "1.8",
            }}
          >
            {news.NewsContent}
          </p>

          {/* Source */}
          <div className="mt-4 p-3 bg-light rounded">
            <strong>Source:</strong> {news.NewsSource || "Unknown"}
          </div>
        </Col>
      </Row>
    </Container>
  );
}
