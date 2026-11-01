import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import {
  Row, Col, Card, Button, Alert, Spinner, Modal, Form, Badge, ListGroup,
} from "react-bootstrap";
import api, { getErrorMessage } from "../api/axiosClient";
import StatusBadge from "../components/StatusBadge";

const formatDate = (d) => (d ? new Date(d).toLocaleString() : "—");

// Các thao tác của editor. noteRequired khớp với requireNote() ở backend
const ACTIONS = {
  approve:          { label: "Approve",         variant: "success",           path: "approve" },
  publish:          { label: "Publish now",     variant: "primary",           path: "publish" },
  "request-changes":{ label: "Request changes", variant: "warning",           path: "request-changes", noteRequired: true },
  reject:           { label: "Reject",          variant: "danger",            path: "reject",          noteRequired: true },
  archive:          { label: "Archive",         variant: "outline-dark",      path: "archive" },
};

// Nút nào hiện ở trạng thái nào (khớp với requireStatus() ở backend)
const ACTIONS_BY_STATUS = {
  PENDING_REVIEW: ["approve", "request-changes", "reject"],
  SCHEDULED: ["publish", "request-changes", "archive"],
  PUBLISHED: ["archive"],
};

// Giá trị cho <input type="datetime-local">: "YYYY-MM-DDTHH:mm" theo giờ máy
function toLocalInput(date) {
  const pad = (n) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
         `T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export default function ReviewArticle() {
  const { id } = useParams();
  const [article, setArticle] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // Popup
  const [action, setAction] = useState(null);      // key trong ACTIONS, null = đóng
  const [note, setNote] = useState("");
  const [mode, setMode] = useState("now");         // approve: "now" | "schedule"
  const [publishDate, setPublishDate] = useState("");
  const [saving, setSaving] = useState(false);
  const [modalError, setModalError] = useState("");

  const load = useCallback(async () => {
    setError("");
    try {
      const [aRes, hRes] = await Promise.all([
        api.get(`review/${id}`),
        api.get(`review/${id}/history`),
      ]);
      setArticle(aRes.data);
      setHistory(hRes.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    setLoading(true);
    load();
  }, [load]);

  const openModal = (key) => {
    setAction(key);
    setNote("");
    setMode("now");
    setPublishDate(toLocalInput(new Date(Date.now() + 60 * 60 * 1000))); // mặc định +1 giờ
    setModalError("");
  };

  const closeModal = () => {
    if (!saving) setAction(null);
  };

  const confirmAction = async () => {
    const cfg = ACTIONS[action];
    if (cfg.noteRequired && !note.trim()) {
      setModalError("Note is required.");
      return;
    }
    if (action === "approve" && mode === "schedule" && new Date(publishDate) <= new Date()) {
      setModalError("Publish time must be in the future.");
      return;
    }

    const body = { note: note.trim() || null };
    if (action === "approve" && mode === "schedule") {
      body.publishDate = publishDate;   // "2026-11-02T09:30" -> LocalDateTime ở backend
    }

    setSaving(true);
    setModalError("");
    try {
      const res = await api.put(`review/${id}/${cfg.path}`, body);
      setAction(null);
      setMessage(`${cfg.label}: article is now ${res.data.status}.`);
      await load();                     // tải lại bài + lịch sử
    } catch (err) {
      setModalError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="text-center py-5"><Spinner animation="border" /></div>;
  }
  if (!article) {
    return (
      <div>
        <Link to="/review">← Back to review queue</Link>
        <Alert variant="danger" className="mt-3">{error || "Article not found"}</Alert>
      </div>
    );
  }

  const available = ACTIONS_BY_STATUS[article.status] || [];
  const cfg = action ? ACTIONS[action] : null;

  return (
    <div>
      <Link to="/review" className="d-inline-block mb-3">← Back to review queue</Link>

      {message && <Alert variant="success" dismissible onClose={() => setMessage("")}>{message}</Alert>}
      {error && <Alert variant="danger" dismissible onClose={() => setError("")}>{error}</Alert>}

      <Row className="g-4">
        {/* ===== Cột trái: nội dung bài ===== */}
        <Col lg={8}>
          <Card>
            <Card.Body>
              <div className="d-flex align-items-center gap-2 mb-2">
                <StatusBadge status={article.status} />
                {article.categoryName && <Badge bg="light" text="dark">{article.categoryName}</Badge>}
              </div>
              <h2 className="fw-bold">{article.title}</h2>
              <div className="text-muted small mb-3">
                By <strong>{article.author?.name}</strong> ({article.author?.email}) ·
                created {formatDate(article.created_at)}
                {article.publish_date && <> · publish date <strong>{formatDate(article.publish_date)}</strong></>}
              </div>

              {article.latest_feedback && (
                <Alert variant="warning" className="py-2">
                  <strong>Latest feedback:</strong> {article.latest_feedback}
                </Alert>
              )}

              {article.thumbnail && (
                <img src={article.thumbnail} alt="" className="img-fluid rounded mb-3" />
              )}

              {/* Text thường, không render HTML -> tránh XSS */}
              <div style={{ whiteSpace: "pre-line", lineHeight: 1.8 }}>
                {article.content || <em className="text-muted">(no content)</em>}
              </div>
            </Card.Body>
          </Card>
        </Col>

        {/* ===== Cột phải: thao tác + lịch sử ===== */}
        <Col lg={4}>
          <Card className="mb-4">
            <Card.Header className="fw-semibold">Actions</Card.Header>
            <Card.Body className="d-grid gap-2">
              {available.length === 0 ? (
                <span className="text-muted">No actions available for this status.</span>
              ) : (
                available.map((key) => (
                  <Button key={key} variant={ACTIONS[key].variant} onClick={() => openModal(key)}>
                    {ACTIONS[key].label}
                  </Button>
                ))
              )}
            </Card.Body>
          </Card>

          <Card>
            <Card.Header className="fw-semibold">History</Card.Header>
            <ListGroup variant="flush">
              {history.length === 0 && <ListGroup.Item className="text-muted">No history.</ListGroup.Item>}
              {[...history].reverse().map((h, i) => (
                <ListGroup.Item key={i}>
                  <div className="d-flex align-items-center gap-1 flex-wrap">
                    {h.fromStatus && <><StatusBadge status={h.fromStatus} /> →</>}
                    <StatusBadge status={h.toStatus} />
                  </div>
                  <div className="small text-muted mt-1">
                    {h.actorName} · {formatDate(h.actionAt)}
                  </div>
                  {h.note && <div className="small mt-1">💬 {h.note}</div>}
                </ListGroup.Item>
              ))}
            </ListGroup>
          </Card>
        </Col>
      </Row>

      {/* ===== Popup xác nhận thao tác ===== */}
      <Modal show={!!action} onHide={closeModal} centered>
        {cfg && (
          <>
            <Modal.Header closeButton>
              <Modal.Title>{cfg.label}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {modalError && <Alert variant="danger" className="py-2">{modalError}</Alert>}

              {action === "approve" && (
                <Form.Group className="mb-3">
                  <Form.Check type="radio" id="mode-now" name="mode" label="Publish now"
                              checked={mode === "now"} onChange={() => setMode("now")} />
                  <Form.Check type="radio" id="mode-schedule" name="mode" label="Schedule"
                              checked={mode === "schedule"} onChange={() => setMode("schedule")} />
                  {mode === "schedule" && (
                    <Form.Control type="datetime-local" className="mt-2" value={publishDate}
                                  min={toLocalInput(new Date())}
                                  onChange={(e) => setPublishDate(e.target.value)} />
                  )}
                </Form.Group>
              )}

              {action === "publish" && (
                <p>Publish this article immediately instead of waiting for the scheduled time?</p>
              )}

              <Form.Group>
                <Form.Label>
                  Note {cfg.noteRequired ? <span className="text-danger">*</span> : <span className="text-muted">(optional)</span>}
                </Form.Label>
                <Form.Control as="textarea" rows={3} maxLength={1000} value={note}
                              onChange={(e) => setNote(e.target.value)}
                              placeholder={cfg.noteRequired ? "Tell the author what to fix..." : ""} />
              </Form.Group>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={closeModal} disabled={saving}>Cancel</Button>
              <Button variant={cfg.variant.replace("outline-", "")} onClick={confirmAction} disabled={saving}>
                {saving ? "Processing..." : cfg.label}
              </Button>
            </Modal.Footer>
          </>
        )}
      </Modal>
    </div>
  );
}