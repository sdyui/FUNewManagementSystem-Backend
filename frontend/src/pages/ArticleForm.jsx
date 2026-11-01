import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router";
import { Form, Button, Alert, Spinner, Row, Col, Card } from "react-bootstrap";
import api, { getErrorMessage } from "../api/axiosClient";
import StatusBadge, { EDITABLE } from "../components/StatusBadge";

const EMPTY = { title: "", content: "", thumbnail: "", categoryId: "", tagIds: [] };

// Dùng chung cho: tạo mới (/my-articles/new), sửa và chỉ xem (/my-articles/:id/edit)
export default function ArticleForm() {
  const { id } = useParams(); // có id = sửa / xem, không có = tạo mới
  const isNew = !id;
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY);
  const [article, setArticle] = useState(null); // dữ liệu gốc (status, feedback...)
  const [categories, setCategories] = useState([]);
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // Bài đã gửi duyệt / đã đăng: tác giả chỉ được xem (AC-ED-001)
  const readOnly = !isNew && article && !EDITABLE.includes(article.status);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const [catRes, tagRes] = await Promise.all([
          api.get("categories/active"),
          // Tag không bắt buộc: lỗi thì form vẫn dùng được, chỉ không chọn tag
          api.get("articles/tags").catch(() => ({ data: [] })),
        ]);
        setCategories(catRes.data);
        setTags(tagRes.data);

        if (isNew) {
          setArticle(null);
          setForm(EMPTY);
        } else {
          const res = await api.get(`articles/${id}`);
          const a = res.data;
          setArticle(a);
          setForm({
            title: a.title || "",
            content: a.content || "",
            thumbnail: a.thumbnail || "",
            categoryId: a.categoryId ?? "",
            tagIds: a.tagIds || [],
          });
        }
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id, isNew]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const toggleTag = (tagId) => {
    setForm((f) => ({
      ...f,
      tagIds: f.tagIds.includes(tagId)
        ? f.tagIds.filter((t) => t !== tagId)
        : [...f.tagIds, tagId],
    }));
  };

  // submitAfter = true: lưu xong gửi duyệt luôn
  const save = async (submitAfter) => {
    setSaving(true);
    setError("");
    const body = {
      title: form.title.trim(),
      content: form.content,
      thumbnail: form.thumbnail.trim() || null,
      categoryId: form.categoryId === "" ? null : Number(form.categoryId),
      tagIds: form.tagIds,
    };
    try {
      const res = isNew
        ? await api.post("articles", body)
        : await api.put(`articles/${id}`, body);

      if (submitAfter) {
        await api.put(`articles/${res.data.id}/submit`);
      }
      navigate("/my-articles");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleSubmitForm = (e) => {
    e.preventDefault();
    save(false);
  };

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" />
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <Link to="/my-articles" className="d-inline-block mb-3">
        ← Back to my articles
      </Link>

      <div className="d-flex align-items-center gap-3 mb-3">
        <h3 className="mb-0">
          {isNew ? "New article" : readOnly ? "View article" : "Edit article"}
        </h3>
        {article && <StatusBadge status={article.status} />}
      </div>

      {/* Ghi chú của editor */}
      {article?.latest_feedback && (
        <Alert variant="warning">
          <strong>Editor feedback:</strong> {article.latest_feedback}
        </Alert>
      )}

      {readOnly && (
        <Alert variant="info">
          This article is <strong>{article.status}</strong> and can no longer be edited.
          {article.publish_date && (
            <> Publish date: {new Date(article.publish_date).toLocaleString()}.</>
          )}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      <Form onSubmit={handleSubmitForm}>
        {/* fieldset disabled: khóa toàn bộ ô nhập khi chỉ xem / đang lưu */}
        <fieldset disabled={readOnly || saving}>
          <Form.Group className="mb-3" controlId="articleTitle">
            <Form.Label>
              Title <span className="text-danger">*</span>
            </Form.Label>
            <Form.Control
              name="title"
              value={form.title}
              onChange={handleChange}
              required
              maxLength={200}
            />
          </Form.Group>

          <Row>
            <Col md={6}>
              <Form.Group className="mb-3" controlId="articleCategory">
                <Form.Label>Category</Form.Label>
                <Form.Select name="categoryId" value={form.categoryId} onChange={handleChange}>
                  <option value="">-- Select category --</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.categoryName}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3" controlId="articleThumbnail">
                <Form.Label>Thumbnail URL</Form.Label>
                <Form.Control
                  name="thumbnail"
                  value={form.thumbnail}
                  onChange={handleChange}
                  maxLength={500}
                  placeholder="https://..."
                />
              </Form.Group>
            </Col>
          </Row>

          {tags.length > 0 && (
          <Form.Group className="mb-3">
            <Form.Label>Tags</Form.Label>
            <div>
              {tags.map((t) => (
                <Form.Check
                  inline
                  key={t.id}
                  type="checkbox"
                  id={`tag-${t.id}`}
                  label={t.tagName}
                  checked={form.tagIds.includes(t.id)}
                  onChange={() => toggleTag(t.id)}
                />
              ))}
            </div>
          </Form.Group>
          )}

          <Form.Group className="mb-3" controlId="articleContent">
            <Form.Label>Content</Form.Label>
            <Form.Control
              as="textarea"
              rows={12}
              name="content"
              value={form.content}
              onChange={handleChange}
            />
            <Form.Text muted>Content is required before submitting for review.</Form.Text>
          </Form.Group>

          {form.thumbnail && (
            <Card className="mb-3" style={{ maxWidth: 320 }}>
              <Card.Img src={form.thumbnail} alt="Thumbnail preview" />
            </Card>
          )}
        </fieldset>

        {!readOnly && (
          <div className="d-flex gap-2">
            <Button type="submit" variant="secondary" disabled={saving}>
              {saving ? "Saving..." : "Save draft"}
            </Button>
            <Button
              variant="primary"
              disabled={saving || !form.content.trim()}
              onClick={() => save(true)}
            >
              Save & submit for review
            </Button>
          </div>
        )}
      </Form>
    </div>
  );
}
