import { useState } from "react";
import { Modal, Button, Form, Alert, Spinner } from "react-bootstrap";
import api, { getErrorMessage } from "../api/axiosClient";

export default function EditEmail({ show, account, onClose, onSaved }) {
  const [email, setEmail] = useState(account?.email ?? "");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  if (!account) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    const value = email.trim();
    if (value === account.email) {
      onClose();
      return;
    }
    setSaving(true);
    setError("");
    try {
      const res = await api.put(`account/${account.accountId}`, {
        accountEmail: value,
      });
      onSaved(res.data);
      onClose();
    } catch (err) {
      console.error(err);
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };
  return (
    <Modal
      show={show}
      onHide={onClose}
      centered
      backdrop={saving ? "static" : true}
    >
      <Form onSubmit={handleSubmit}>
        <Modal.Header closeButton={!saving}>
          <Modal.Title>Change Email</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          {error && <Alert variant="danger">{error}</Alert>}

          <Form.Group className="mb-3">
            <Form.Label>Account</Form.Label>
            <Form.Control value={account?.accountName ?? ""} disabled />
          </Form.Group>

          <Form.Group>
            <Form.Label>New email</Form.Label>
            <Form.Control
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoFocus
              disabled={saving}
            />
          </Form.Group>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={onClose} disabled={saving}>
            Hủy
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={saving || !email.trim()}
          >
            {saving ? (
              <>
                <Spinner size="sm" className="me-2" />
                saving...
              </>
            ) : (
              "Lưu"
            )}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}
