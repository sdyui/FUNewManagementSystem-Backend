import { useState, useEffect } from "react";
import {
  Container,
  Accordion,
  Table,
  Button,
  Form,
  Modal,
  Badge,
  Alert,
  Row,
  Col,
} from "react-bootstrap";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1/";
const CATEGORY_API = `${API_BASE_URL}categories`;

export default function CategoryList() {
  // State lưu danh sách category hiển thị trên bảng
  const [categories, setCategories] = useState([]);
  // State lưu danh sách category đang hoạt động (để chọn Parent Category)
  const [activeCategories, setActiveCategories] = useState([]);

  // State cho ô tìm kiếm
  const [searchName, setSearchName] = useState("");

  // State cho Modal Thêm / Sửa
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [categoryName, setCategoryName] = useState("");
  const [categoryDescription, setCategoryDescription] = useState("");
  const [parentCategoryId, setParentCategoryId] = useState("");
  const [isActive, setIsActive] = useState(true);

  // State hiển thị thông báo thành công / lỗi
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Hàm lấy Header có kèm JWT Token từ localStorage
  const getHeaders = () => {
    let token = localStorage.getItem("accessToken");
    const userStr = localStorage.getItem("user");
    if (!token && userStr) {
      try {
        const userObj = JSON.parse(userStr);
        token = userObj.accessToken || userObj.token;
      } catch {
        // ignore JSON parse error
      }
    }
    const headers = {
      "Content-Type": "application/json",
    };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
  };

  // 1. Lấy danh sách Category từ Backend (hỗ trợ tìm kiếm theo tên)
  const fetchCategories = async () => {
    setError("");
    try {
      let url = `${CATEGORY_API}?size=50`;
      if (searchName.trim() !== "") {
        url += `&name=${encodeURIComponent(searchName.trim())}`;
      }
      const response = await fetch(url, {
        headers: getHeaders(),
      });
      const data = await response.json();
      if (response.ok) {
        setCategories(data.content || []);
      } else {
        setError(data.message || "Failed to load categories.");
      }
    } catch {
      setError("Cannot connect to Backend server.");
    }
  };

  // 2. Lấy danh sách Category đang active để làm dropdown chọn danh mục cha
  const fetchActiveCategories = async () => {
    try {
      const response = await fetch(`${CATEGORY_API}/active`, {
        headers: getHeaders(),
      });
      if (response.ok) {
        const data = await response.json();
        setActiveCategories(data || []);
      }
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    fetchCategories();
    fetchActiveCategories();
  }, []);

  // 3. Mở Modal Tạo mới
  const handleOpenCreate = () => {
    setEditingId(null);
    setCategoryName("");
    setCategoryDescription("");
    setParentCategoryId("");
    setIsActive(true);
    setError("");
    setShowModal(true);
  };

  // 4. Mở Modal Chỉnh sửa
  const handleOpenEdit = (cat) => {
    setEditingId(cat.id);
    setCategoryName(cat.categoryName || "");
    setCategoryDescription(cat.categoryDescription || "");
    setParentCategoryId(cat.parentCategoryId ? String(cat.parentCategoryId) : "");
    setIsActive(cat.isActive !== undefined ? cat.isActive : true);
    setError("");
    setShowModal(true);
  };

  // 5. Lưu Category (Tạo mới hoặc Cập nhật)
  const handleSave = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    const requestBody = {
      categoryName: categoryName,
      categoryDescription: categoryDescription,
      parentCategoryId:
        parentCategoryId === "" ? null : Number(parentCategoryId),
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

      const resData = await response.json();

      if (response.ok) {
        if (editingId === null) {
          setMessage("Created category successfully!");
        } else {
          setMessage("Updated category successfully!");
        }
        setShowModal(false);
        fetchCategories();
        fetchActiveCategories();
      } else {
        setError(resData.message || "Failed to save category.");
      }
    } catch {
      setError("Error connecting to server.");
    }
  };

  // 6. Xóa Category
  const handleDelete = async (id) => {
    const confirmDelete = window.confirm(
      "Are you sure you want to delete this category?"
    );
    if (!confirmDelete) {
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
        setMessage("Deleted category successfully!");
        fetchCategories();
        fetchActiveCategories();
      } else {
        const data = await response.json();
        setError(data.message || "Cannot delete this category.");
      }
    } catch {
      setError("Error connecting to server.");
    }
  };

  return (
    <Container className="mt-4">
      <h2 className="mb-3">Category Management</h2>

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

      {/* Thanh công cụ Tìm kiếm & Thêm mới dùng Accordion của React-Bootstrap */}
      <Accordion defaultActiveKey="0" className="mb-4">
        <Accordion.Item eventKey="0">
          <Accordion.Header>Search & Actions</Accordion.Header>
          <Accordion.Body>
            <Form
              onSubmit={(e) => {
                e.preventDefault();
                fetchCategories();
              }}
            >
              <Row className="align-items-center g-2">
                <Col md={8}>
                  <Form.Control
                    type="text"
                    placeholder="Search by category name..."
                    value={searchName}
                    onChange={(e) => setSearchName(e.target.value)}
                  />
                </Col>
                <Col md={4} className="d-flex gap-2">
                  <Button variant="primary" type="submit">
                    Search
                  </Button>
                  <Button variant="success" onClick={handleOpenCreate}>
                    + Add Category
                  </Button>
                </Col>
              </Row>
            </Form>
          </Accordion.Body>
        </Accordion.Item>
      </Accordion>

      {/* Bảng danh sách Category */}
      <Table striped bordered hover responsive>
        <thead>
          <tr>
            <th>#</th>
            <th>ID</th>
            <th>Category Name</th>
            <th>Description</th>
            <th>Parent Category</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {categories.length > 0 ? (
            categories.map((cat, index) => (
              <tr key={cat.id}>
                <td>{index + 1}</td>
                <td>{cat.id}</td>
                <td>
                  <strong>{cat.categoryName}</strong>
                </td>
                <td>{cat.categoryDescription}</td>
                <td>{cat.parentCategoryName || "Root"}</td>
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
                    className="me-2"
                    onClick={() => handleOpenEdit(cat)}
                  >
                    Edit
                  </Button>
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
              <td colSpan="7" className="text-center">
                No categories found.
              </td>
            </tr>
          )}
        </tbody>
      </Table>

      {/* Modal Thêm / Sửa Category */}
      <Modal show={showModal} onHide={() => setShowModal(false)} centered>
        <Form onSubmit={handleSave}>
          <Modal.Header closeButton>
            <Modal.Title>
              {editingId === null ? "Add Category" : "Edit Category"}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Category Name</Form.Label>
              <Form.Control
                type="text"
                required
                placeholder="Enter category name"
                value={categoryName}
                onChange={(e) => setCategoryName(e.target.value)}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                placeholder="Enter description"
                value={categoryDescription}
                onChange={(e) => setCategoryDescription(e.target.value)}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Parent Category</Form.Label>
              <Form.Select
                value={parentCategoryId}
                onChange={(e) => setParentCategoryId(e.target.value)}
              >
                <option value="">-- Root Category (None) --</option>
                {activeCategories
                  .filter((c) => c.id !== editingId)
                  .map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.categoryName}
                    </option>
                  ))}
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
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
              Cancel
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
