import { useState, useEffect } from "react";
import {
  Container,
  Table,
  Button,
  Form,
  Modal,
  Badge,
  Alert,
  Accordion,
} from "react-bootstrap";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1/";
const CATEGORY_API = `${API_BASE_URL}categories`;

export default function ManageCategory() {
  // 1. State danh sách Category
  const [categories, setCategories] = useState([]);
  const [activeCategories, setActiveCategories] = useState([]);

  // 2. State tìm kiếm
  const [searchName, setSearchName] = useState("");

  // 3. State cho Modal Thêm / Sửa
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [categoryName, setCategoryName] = useState("");
  const [categoryDescription, setCategoryDescription] = useState("");
  const [parentCategoryId, setParentCategoryId] = useState("");
  const [isActive, setIsActive] = useState(true);

  // 4. State hiển thị thông báo
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Hàm lấy Token từ localStorage (do Login.jsx lưu trong key "user")
  const getHeaders = () => {
    let token = localStorage.getItem("accessToken");
    if (!token) {
      const savedUser = localStorage.getItem("user");
      if (savedUser) {
        const parsed = JSON.parse(savedUser);
        token = parsed.accessToken;
      }
    }
    return {
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : "",
    };
  };

  // Tải danh sách Category từ Backend
  const fetchCategories = async () => {
    try {
      const url = searchName
        ? `${CATEGORY_API}?name=${encodeURIComponent(searchName)}&size=50`
        : `${CATEGORY_API}?size=50`;

      const response = await fetch(url, { headers: getHeaders() });
      const data = await response.json();

      if (response.ok) {
        setCategories(data.content || []);
      } else {
        setError(data.message || "Không thể tải danh sách danh mục (Hãy đăng nhập tài khoản ADMIN)");
      }
    } catch (err) {
      setError("Lỗi kết nối tới Backend");
    }
  };

  // Tải danh sách Category đang Active cho ô chọn Danh mục cha
  const fetchActiveCategories = async () => {
    try {
      const response = await fetch(`${CATEGORY_API}/active`, {
        headers: getHeaders(),
      });
      if (response.ok) {
        const data = await response.json();
        setActiveCategories(data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchCategories();
    fetchActiveCategories();
  }, []);

  // Mở Modal để Thêm mới
  const handleOpenCreate = () => {
    setEditingId(null);
    setCategoryName("");
    setCategoryDescription("");
    setParentCategoryId("");
    setIsActive(true);
    setError("");
    setShowModal(true);
  };

  // Mở Modal để Sửa
  const handleOpenEdit = (cat) => {
    setEditingId(cat.id);
    setCategoryName(cat.categoryName);
    setCategoryDescription(cat.categoryDescription || "");
    setParentCategoryId(cat.parentCategoryId || "");
    setIsActive(cat.isActive);
    setError("");
    setShowModal(true);
  };

  // Lưu Danh mục (Thêm mới hoặc Cập nhật)
  const handleSave = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    const requestBody = {
      categoryName: categoryName,
      categoryDescription: categoryDescription,
      parentCategoryId: parentCategoryId ? Number(parentCategoryId) : null,
      isActive: isActive,
    };

    try {
      let response;
      if (editingId === null) {
        response = await fetch(CATEGORY_API, {
          method: "POST",
          headers: getHeaders(),
          body: JSON.stringify(requestBody),
        });
      } else {
        response = await fetch(`${CATEGORY_API}/${editingId}`, {
          method: "PUT",
          headers: getHeaders(),
          body: JSON.stringify(requestBody),
        });
      }

      const data = await response.json();

      if (response.ok) {
        setMessage(
          editingId === null
            ? "Tạo danh mục thành công!"
            : "Cập nhật danh mục thành công!"
        );
        setShowModal(false);
        fetchCategories();
        fetchActiveCategories();
      } else {
        setError(data.message || "Có lỗi xảy ra khi lưu danh mục");
      }
    } catch (err) {
      setError("Lỗi kết nối Backend");
    }
  };

  // Xóa Danh mục
  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa danh mục này?")) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const response = await fetch(`${CATEGORY_API}/${id}`, {
        method: "DELETE",
        headers: getHeaders(),
      });

      if (response.ok) {
        setMessage("Xóa danh mục thành công!");
        fetchCategories();
        fetchActiveCategories();
      } else {
        const data = await response.json();
        setError(data.message || "Không thể xóa danh mục này");
      }
    } catch (err) {
      setError("Lỗi kết nối Backend");
    }
  };

  return (
    <Container>
      <h2>Manage Categories</h2>

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

      {/* Component Accordion từ trang tài liệu React-Bootstrap */}
      <Accordion defaultActiveKey="0">
        <Accordion.Item eventKey="0">
          <Accordion.Header>Tìm kiếm & Thêm mới Danh mục</Accordion.Header>
          <Accordion.Body>
            <Form
              onSubmit={(e) => {
                e.preventDefault();
                fetchCategories();
              }}
            >
              <Form.Group controlId="searchName">
                <Form.Label>Tên danh mục</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Nhập tên danh mục cần tìm..."
                  value={searchName}
                  onChange={(e) => setSearchName(e.target.value)}
                />
              </Form.Group>
              <br />
              <Button variant="primary" type="submit">
                Search
              </Button>{" "}
              <Button variant="success" onClick={handleOpenCreate}>
                + Add Category
              </Button>
            </Form>
          </Accordion.Body>
        </Accordion.Item>
      </Accordion>

      <br />

      {/* Bảng hiển thị danh sách Category */}
      <Table striped bordered hover>
        <thead>
          <tr>
            <th>#</th>
            <th>Category ID</th>
            <th>Category Name</th>
            <th>Description</th>
            <th>Parent Category</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {categories.length > 0 ? (
            categories.map((cat, index) => (
              <tr key={cat.id}>
                <td>{index + 1}</td>
                <td>{cat.id}</td>
                <td>{cat.categoryName}</td>
                <td>{cat.categoryDescription}</td>
                <td>{cat.parentCategoryName || "Root Category"}</td>
                <td>
                  {cat.isActive ? (
                    <Badge bg="success">Active</Badge>
                  ) : (
                    <Badge bg="secondary">Inactive</Badge>
                  )}
                </td>
                <td>
                  <Button
                    variant="warning"
                    size="sm"
                    onClick={() => handleOpenEdit(cat)}
                  >
                    Edit
                  </Button>{" "}
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => handleDelete(cat.id)}
                  >
                    Delete
                  </Button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7">Không tìm thấy danh mục nào.</td>
            </tr>
          )}
        </tbody>
      </Table>

      {/* Modal Form dùng cho Thêm mới và Cập nhật */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Form onSubmit={handleSave}>
          <Modal.Header closeButton>
            <Modal.Title>
              {editingId === null ? "Add Category" : "Edit Category"}
            </Modal.Title>
          </Modal.Header>

          <Modal.Body>
            <Form.Group controlId="categoryName">
              <Form.Label>Category Name *</Form.Label>
              <Form.Control
                type="text"
                required
                value={categoryName}
                onChange={(e) => setCategoryName(e.target.value)}
              />
            </Form.Group>

            <br />

            <Form.Group controlId="categoryDescription">
              <Form.Label>Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={categoryDescription}
                onChange={(e) => setCategoryDescription(e.target.value)}
              />
            </Form.Group>

            <br />

            <Form.Group controlId="parentCategoryId">
              <Form.Label>Parent Category</Form.Label>
              <Form.Select
                value={parentCategoryId}
                onChange={(e) => setParentCategoryId(e.target.value)}
              >
                <option value="">-- Root Category (Không có) --</option>
                {activeCategories.map((parent) => (
                  <option key={parent.id} value={parent.id}>
                    {parent.categoryName}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>

            <br />

            <Form.Group controlId="isActive">
              <Form.Check
                type="checkbox"
                label="Active"
                checked={isActive}
                onChange={(e) => setIsActive(e.target.checked)}
              />
            </Form.Group>
          </Modal.Body>

          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowModal(false)}>
              Close
            </Button>
            <Button variant="primary" type="submit">
              Save
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Container>
  );
}
