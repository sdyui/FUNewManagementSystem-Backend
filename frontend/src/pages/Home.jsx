import { useEffect, useState } from "react";
import { Link } from "react-router";
import {
  Container,
  Row,
  Col,
  Card,
  Form,
  Button,
  InputGroup,
  Spinner,
  Alert,
} from "react-bootstrap";
import api, { getErrorMessage } from "../api/axiosClient";
import { getUser, getRole, HOME_BY_ROLE } from "../components/Auth/auth";

const PAGE_SIZE = 9;

export default function Home() {
  const [items, setItems] = useState([]); // danh sách bài
  const [page, setPage] = useState(0); // trang hiện tại (backend đếm từ 0)
  const [totalPages, setTotalPages] = useState(0);
  const [keyword, setKeyword] = useState(""); // chữ đang gõ trong ô search
  const [title, setTitle] = useState(""); // từ khóa đã bấm Search
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const user = getUser();

  // Gọi API mỗi khi đổi trang hoặc đổi từ khóa
  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get("public/news", {
          params: {
            title: title || undefined, // rỗng thì không gửi tham số
            page,
            size: PAGE_SIZE,
            sort: "publishDate,desc",
          },
        });
        setItems(res.data.content);
        setTotalPages(res.data.totalPages);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [page, title]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0); // search mới thì quay về trang đầu
    setTitle(keyword.trim());
  };

  return (
    <Container className="py-4">
      {/* Logo + nút Login / Dashboard */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <img src="/images/logo.png" alt="FU News" style={{ height: 56 }} />
        {user ? (
          <Button
            as={Link}
            to={HOME_BY_ROLE[getRole()] || "/news"}
            variant="outline-primary"
          >
            Dashboard
          </Button>
        ) : (
          <Button as={Link} to="/login">
            Login
          </Button>
        )}
      </div>

      {/* Ô search */}
      <Form onSubmit={handleSearch} className="mb-4">
        <InputGroup>
          <Form.Control
            placeholder="Search news by title..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <Button type="submit">Search</Button>
        </InputGroup>
      </Form>

      {error && <Alert variant="danger">{error}</Alert>}

      {/* Danh sách bài dạng thẻ */}
      {loading ? (
        <div className="text-center py-5">
          <Spinner animation="border" />
        </div>
      ) : items.length === 0 ? (
        <p className="text-center text-muted py-5">No news found.</p>
      ) : (
        <Row xs={1} md={2} lg={3} className="g-4">
          {items.map((n) => (
            <Col key={n.id}>
              <Card className="h-100 shadow-sm">
                {n.thumbnail && (
                  <Card.Img
                    variant="top"
                    src={n.thumbnail}
                    style={{ height: 180, objectFit: "cover" }}
                  />
                )}
                <Card.Body className="d-flex flex-column">
                  {n.categoryName && (
                    <small className="text-primary fw-semibold mb-1">
                      {n.categoryName}
                    </small>
                  )}
                  <Card.Title>{n.title}</Card.Title>
                  {n.headline && (
                    <Card.Text className="text-muted">{n.headline}</Card.Text>
                  )}

                  <div className="mt-auto d-flex justify-content-between align-items-center">
                    <small className="text-muted">
                      {n.publishDate
                        ? new Date(n.publishDate).toLocaleDateString()
                        : ""}
                    </small>
                    <Button
                      as={Link}
                      to={`/news/${n.id}`}
                      size="sm"
                      variant="outline-primary"
                    >
                      Read more
                    </Button>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      {/* Phân trang */}
      {totalPages > 1 && (
        <div className="d-flex justify-content-center align-items-center gap-3 mt-4">
          <Button
            variant="outline-secondary"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            ← Prev
          </Button>
          <span>
            Page {page + 1} / {totalPages}
          </span>
          <Button
            variant="outline-secondary"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next →
          </Button>
        </div>
      )}
    </Container>
  );
}
