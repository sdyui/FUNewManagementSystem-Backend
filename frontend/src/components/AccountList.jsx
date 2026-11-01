import { useState, useEffect } from "react";
import { Container, Table, Button, Form, InputGroup } from "react-bootstrap";
import { Link } from "react-router";
import api from "../api/axiosClient";
import EditEmail from "./EditEmail";
export default function ManageAccount() {
  const [searchTerm, setSearchTerm] = useState("");
  const [accounts, setAccounts] = useState([]);
  const [editing, setEditing] = useState(null);
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

          {/* Add Account */}
          <Button
            as={Link}
            to="/accounts/add"
            variant="success"
            className="text-nowrap"
          >
            + Add Account
          </Button>
        </div>
      </div>

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

                <td>{item.accountRole}</td>

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

                    <Button variant="danger" size="sm" className="flex-fill">
                      Delete
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
