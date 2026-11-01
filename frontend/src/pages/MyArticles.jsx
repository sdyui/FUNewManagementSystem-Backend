import { useEffect, useState } from "react";
import { Link } from "react-router";
import { Table, Button, Nav, Alert, Spinner } from "react-bootstrap";
import api, { getErrorMessage } from "../api/axiosClient";
import StatusBadge, { STATUS, EDITABLE } from "../components/StatusBadge";

const PAGE_SIZE = 10;
const formatDate = (d) => (d ? new Date(d).toLocaleString() : "—");

export default function MyArticles() {
  const [status, setStatus] = useState(""); // "" = tất cả
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [reload, setReload] = useState(0); // tăng lên để tải lại bảng

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get("articles/my", {
          params: {
            status: status || undefined,
            page,
            size: PAGE_SIZE,
            sort: "createdDate,desc",
          },
        });
        setData(res.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [status, page, reload]);

  const changeTab = (s) => {
    setStatus(s);
    setPage(0);
  };

  const handleSubmit = async (article) => {
    if (!window.confirm(`Submit "${article.title}" for review?`)) return;
    setError("");
    setMessage("");
    try {
      const res = await api.put(`articles/${article.id}/submit`);
      setMessage(res.data.message);
      setReload((r) => r + 1);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">My articles</h3>
        <Button as={Link} to="/my-articles/new" variant="success">
          + New article
        </Button>
      </div>

      {/* Tab lọc theo trạng thái */}
      <Nav
        variant="tabs"
        activeKey={status}
        onSelect={(k) => changeTab(k)}
        className="mb-3"
      >
        <Nav.Item>
          <Nav.Link eventKey="">All</Nav.Link>
        </Nav.Item>
        {Object.entries(STATUS).map(([key, s]) => (
          <Nav.Item key={key}>
            <Nav.Link eventKey={key}>{s.label}</Nav.Link>
          </Nav.Item>
        ))}
      </Nav>

      {message && (
        <Alert variant="success" dismissible onClose={() => setMessage("")}>
          {message}
        </Alert>
      )}
      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      {loading ? (
        <div className="text-center py-5">
          <Spinner animation="border" />
        </div>
      ) : (
        <Table hover responsive className="align-middle">
          <thead className="table-light">
            <tr>
              <th>Title</th>
              <th>Category</th>
              <th>Status</th>
              <th>Updated</th>
              <th style={{ width: 200 }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {data.content.length === 0 ? (
              <tr>
                <td colSpan="5" className="text-center text-muted py-4">
                  No articles.
                </td>
              </tr>
            ) : (
              data.content.map((a) => {
                const editable = EDITABLE.includes(a.status);
                return (
                  <tr key={a.id}>
                    <td>
                      <strong>{a.title}</strong>
                      {/* Ghi chú của editor khi bị trả về / từ chối */}
                      {a.latest_feedback && (
                        <div className="small text-danger mt-1">
                          Editor: {a.latest_feedback}
                        </div>
                      )}
                    </td>
                    <td>{a.categoryName || "—"}</td>
                    <td>
                      <StatusBadge status={a.status} />
                    </td>
                    <td className="small">
                      {formatDate(a.updated_at || a.created_at)}
                    </td>
                    <td>
                      <div className="d-flex gap-2">
                        <Button
                          as={Link}
                          to={`/my-articles/${a.id}/edit`}
                          size="sm"
                          variant={editable ? "warning" : "outline-secondary"}
                        >
                          {editable ? "Edit" : "View"}
                        </Button>
                        {editable && (
                          <Button
                            size="sm"
                            variant="primary"
                            onClick={() => handleSubmit(a)}
                          >
                            Submit
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </Table>
      )}

      {data.totalPages > 1 && (
        <div className="d-flex justify-content-center align-items-center gap-3">
          <Button
            variant="outline-secondary"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            ← Prev
          </Button>
          <span>
            Page {page + 1} / {data.totalPages}
          </span>
          <Button
            variant="outline-secondary"
            size="sm"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next →
          </Button>
        </div>
      )}
    </div>
  );
}
