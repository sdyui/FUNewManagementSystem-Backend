import { useEffect, useState } from "react";
import { Link } from "react-router";
import { Table, Button, Form, Row, Col, Alert, Spinner } from "react-bootstrap";
import api, { getErrorMessage } from "../api/axiosClient";
import StatusBadge, { STATUS } from "../components/StatusBadge";

const PAGE_SIZE = 20;
const EMPTY_FILTER = { keyword: "", actor: "", toStatus: "", from: "", to: "" };
const formatDate = (d) => (d ? new Date(d).toLocaleString() : "—");

export default function AuditLogs() {
  const [form, setForm] = useState(EMPTY_FILTER); // các ô đang nhập
  const [filter, setFilter] = useState(EMPTY_FILTER); // bộ lọc đã bấm Search
  const [page, setPage] = useState(0);
  const [data, setData] = useState({
    content: [],
    totalPages: 0,
    totalElements: 0,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        // Bỏ các ô trống để không gửi "keyword=" lên backend
        const params = { page, size: PAGE_SIZE };
        Object.entries(filter).forEach(([k, v]) => {
          if (v) params[k] = v;
        });
        const res = await api.get("review/logs", { params });
        setData(res.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [filter, page]);

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setFilter(form);
  };

  const handleReset = () => {
    setForm(EMPTY_FILTER);
    setFilter(EMPTY_FILTER);
    setPage(0);
  };

  return (
    <div>
      <h3 className="mb-1">Audit logs</h3>
      <p className="text-muted mb-3">
        {data.totalElements} record(s) · read-only
      </p>

      {/* Bộ lọc */}
      <Form onSubmit={handleSearch} className="mb-3">
        <Row className="g-2 align-items-end">
          <Col md={3}>
            <Form.Label className="small mb-1">Article title</Form.Label>
            <Form.Control
              size="sm"
              name="keyword"
              value={form.keyword}
              onChange={handleChange}
            />
          </Col>
          <Col md={2}>
            <Form.Label className="small mb-1">Actor</Form.Label>
            <Form.Control
              size="sm"
              name="actor"
              value={form.actor}
              onChange={handleChange}
            />
          </Col>
          <Col md={2}>
            <Form.Label className="small mb-1">To status</Form.Label>
            <Form.Select
              size="sm"
              name="toStatus"
              value={form.toStatus}
              onChange={handleChange}
            >
              <option value="">All</option>
              {Object.entries(STATUS).map(([key, s]) => (
                <option key={key} value={key}>
                  {s.label}
                </option>
              ))}
            </Form.Select>
          </Col>
          <Col md={2}>
            <Form.Label className="small mb-1">From</Form.Label>
            <Form.Control
              size="sm"
              type="date"
              name="from"
              value={form.from}
              onChange={handleChange}
            />
          </Col>
          <Col md={2}>
            <Form.Label className="small mb-1">To</Form.Label>
            <Form.Control
              size="sm"
              type="date"
              name="to"
              value={form.to}
              min={form.from || undefined}
              onChange={handleChange}
            />
          </Col>
          <Col md={1} className="d-flex gap-1">
            <Button size="sm" type="submit">
              Search
            </Button>
            <Button
              size="sm"
              variant="outline-secondary"
              onClick={handleReset}
              title="Reset"
            >
              ↺
            </Button>
          </Col>
        </Row>
      </Form>

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
        <Table hover responsive size="sm" className="align-middle">
          <thead className="table-light">
            <tr>
              <th style={{ width: 170 }}>Time</th>
              <th>Article</th>
              <th style={{ width: 280 }}>Change</th>
              <th style={{ width: 120 }}>Actor</th>
              <th>Note</th>
            </tr>
          </thead>
          <tbody>
            {data.content.length === 0 ? (
              <tr>
                <td colSpan="5" className="text-center text-muted py-4">
                  No audit logs.
                </td>
              </tr>
            ) : (
              data.content.map((l) => (
                <tr key={l.id}>
                  <td className="small">{formatDate(l.actionAt)}</td>
                  <td>
                    <Link
                      to={`/review/${l.articleId}`}
                      className="text-decoration-none"
                    >
                      {l.articleTitle}
                    </Link>
                  </td>
                  <td>
                    {/* fromStatus -> toStatus */}
                    <div className="d-flex align-items-center gap-1 flex-wrap">
                      {l.fromStatus ? (
                        <StatusBadge status={l.fromStatus} />
                      ) : (
                        <span className="text-muted small">(none)</span>
                      )}
                      <span>→</span>
                      <StatusBadge status={l.toStatus} />
                    </div>
                  </td>
                  <td>
                    <span className="badge bg-light text-dark border">
                      {l.actorName}
                    </span>
                  </td>
                  <td className="small text-muted">{l.note || "—"}</td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      )}

      {/* Phân trang */}
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
