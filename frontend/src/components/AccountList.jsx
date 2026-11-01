import { useState, useEffect } from "react";
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

  const ROLES = ["STAFF", "EDITOR", "ADMIN"];

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
  useEffect(() => {
    async function fetchAccounts() {
      try {
        const response = await api.get("account");
        // console.log("Fetched accounts:", response.data);
        setAccounts(response.data.content ?? []);
      } catch (error) {
        console.error("Error fetching accounts:", error);
      }
    }
    fetchAccounts();
  }, []);

  const handleEmailSaved = (updated) => {
    setAccounts((prev) =>
      prev.map((a) =>
        a.accountId === updated.accountId ? { ...a, email: updated.email } : a,
      ),
    );
  };
  return (
    <Container className="mt-4">
      {/* Header */}
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <h2 className="mb-0">Manage Accounts</h2>

        <div className="d-flex gap-2">
          {/* Search */}
          <InputGroup style={{ maxWidth: "400px" }}>
            <Form.Control
              placeholder="Tìm kiếm tài khoản..."
              aria-label="Tìm kiếm tài khoản"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />

            <Button variant="primary">Search</Button>
          </InputGroup>

        
        </div>
      </div>

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

      {/* Account Table */}
      <Table striped bordered hover responsive>
        <thead className="table-dark">
          <tr>
            <th>#</th>
            <th>Account ID</th>
            <th>Account Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {accountList.length > 0 ? (
            accountList.map((item, index) => (
              <tr key={item.accountId}>
                <td>{index + 1}</td>

                <td>{item.accountId}</td>

                <td>
                  <strong>{item.accountName}</strong>
                </td>

                <td>{item.email}</td>

                <td style={{ minWidth: 140 }}>
                  {item.accountId === myId ? (
                    <span title="You cannot change your own role">
                      {item.accountRole} <small className="text-muted">(you)</small>
                    </span>
                  ) : (
                    <Form.Select
                      size="sm"
                      value={item.accountRole}
                      onChange={(e) => handleRoleChange(item, e.target.value)}
                    >
                      {ROLES.map((r) => (
                        <option key={r} value={r}>
                          {r}
                        </option>
                      ))}
                    </Form.Select>
                  )}
                </td>

                <td>
                  <div className="d-flex gap-2">
                    <Button
                      variant="warning"
                      size="sm"
                      className="flex-fill"
                      onClick={() => setEditing(item)}
                    >
                      Edit
                    </Button>

                    
                  </div>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="6" className="text-center">
                Không tìm thấy tài khoản nào.
              </td>
            </tr>
          )}
        </tbody>
      </Table>

      <EditEmail
        key={editing?.accountId ?? "none"}
        show={editing !== null}
        account={editing}
        onClose={() => setEditing(null)}
        onSaved={handleEmailSaved}
      />
    </Container>
  );
}
