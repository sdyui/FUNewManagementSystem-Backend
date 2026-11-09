import { useState } from "react";
import { Container, Table, Button, Form, InputGroup, Alert } from "react-bootstrap";
import api, { getErrorMessage } from "../api/axiosClient";
import EditEmail from "./EditEmail";
import { getUser } from "./Auth/auth";
export default function ManageAccount() {
  const [searchTerm, setSearchTerm] = useState("");
  const [accounts, setAccounts] = useState([]);
  const [editing, setEditing] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const myId = getUser()?.account?.accountId; // không cho tự đổi role của mình

  const ROLES = ["CONTRIBUTOR", "STAFF", "EDITOR", "ADMIN"];

  const handleRoleChange = async (item, newRole) => {
    if (newRole === item.accountRole) return;
    if (!window.confirm(`Change role of "${item.accountName}" from ${item.accountRole} to ${newRole}?`)) {
      return;
    }
    setError("");
    setMessage("");
    try {
      const res = await api.put(`account/${item.accountId}/role`, { accountRole: newRole });
      // Cập nhật lại đúng dòng đó trong bảng, không cần tải lại cả danh sách
      setAccounts((prev) =>
        prev.map((a) =>
          a.accountId === item.accountId ? { ...a, accountRole: res.data.accountRole } : a,
        ),
      );
      setMessage(`${item.accountName} is now ${res.data.accountRole}.`);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  // Search account
  let accountList = accounts;

  if (searchTerm.trim() !== "") {
    const keyword = searchTerm.toLowerCase();

    accountList = accountList.filter(
      (item) =>
        item.accountName?.toLowerCase().includes(keyword) ||
        item.accountEmail?.toLowerCase().includes(keyword) ||
        item.accountRole?.toLowerCase().includes(keyword),
    );
  }

  return (
    <Container className="mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="mb-0">Account Management</h2>
        <EditEmail
          editing={editing}
          onSuccess={() => {
            setEditing(null);
            setMessage("Account updated successfully.");
          }}
          onClose={() => setEditing(null)}
        />
      </div>

      {message && (
        <Alert variant="success" onClose={() => setMessage("")} dismissible>
          {message}
        </Alert>
      )}
      {error && (
        <Alert variant="danger" onClose={() => setError("")} dismissible>
          {error}
        </Alert>
      )}

      {/* Search Bar */}
      <InputGroup className="mb-3">
        <Form.Control
          type="text"
          placeholder="Search by name, email, role..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        {searchTerm && (
          <Button variant="outline-secondary" onClick={() => setSearchTerm("")}>
            Clear
          </Button>
        )}
      </InputGroup>

      {/* Accounts Table */}
      <Table striped bordered hover responsive>
        <thead>
          <tr>
            <th>#</th>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {accountList.length > 0 ? (
            accountList.map((item, index) => {
              const isMe = item.accountId === myId;
              return (
                <tr key={item.accountId}>
                  <td>{index + 1}</td>
                  <td>{item.accountId}</td>
                  <td>
                    <strong>{item.accountName}</strong>
                    {isMe && <span className="badge bg-secondary ms-2">You</span>}
                  </td>
                  <td>{item.accountEmail}</td>
                  <td>
                    {/* Role dropdown: không cho tự đổi role của chính mình */}
                    <Form.Select
                      size="sm"
                      value={item.accountRole}
                      disabled={isMe}
                      onChange={(e) => void handleRoleChange(item, e.target.value)}
                      style={{ width: "150px", display: "inline-block" }}
                    >
                      {ROLES.map((r) => (
                        <option key={r} value={r}>
                          {r}
                        </option>
                      ))}
                    </Form.Select>
                  </td>
                  <td>
                    <Button
                      variant="warning"
                      size="sm"
                      className="me-2"
                      onClick={() => setEditing(item)}
                    >
                      Edit
                    </Button>
                  </td>
                </tr>
              );
            })
          ) : (
            <tr>
              <td colSpan="6" className="text-center">
                No accounts found.
              </td>
            </tr>
          )}
        </tbody>
      </Table>
    </Container>
  );
}
