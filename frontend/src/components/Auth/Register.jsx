import { useState } from "react";
import { useNavigate, Link } from "react-router";
import axios from "axios";
import { Form, Button, Card } from "react-bootstrap";
import Alert from "react-bootstrap/Alert";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function Register() {
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    accountName: "",
    accountEmail: "",
    accountPassword: "",
    accountConfirmPassword: "",
  });
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (form.accountPassword !== form.accountConfirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setLoading(true);
    try {
      await axios.post(`${API_BASE_URL}account`, {
        accountName: form.accountName,
        accountEmail: form.accountEmail,
        accountPassword: form.accountPassword,
        comfirmAccountPassword: form.accountConfirmPassword,
      });
      void navigate("/login");
    } catch (err) {
      if (err.response?.status === 400) {
        setError("Account name or email already exists.");
      } else {
        setError(
          err.response?.data?.message ||
            "An error occurred. Please try again later."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="d-flex justify-content-center align-items-center"
      style={{ minHeight: "80vh" }}
    >
      <Card
        className="shadow-sm border-0"
        style={{ width: "100%", maxWidth: "450px" }}
      >
        <Card.Body className="p-4">
          <Card.Title className="text-center mb-4">Register</Card.Title>
          <Form onSubmit={(e) => void handleSubmit(e)}>
            <Form.Group className="mb-3">
              <Form.Label>Account Name</Form.Label>
              <Form.Control
                type="text"
                name="accountName"
                value={form.accountName}
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Account Email</Form.Label>
              <Form.Control
                type="email"
                name="accountEmail"
                value={form.accountEmail}
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Account Password</Form.Label>
              <Form.Control
                type="password"
                name="accountPassword"
                minLength={6}
                value={form.accountPassword}
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Confirm Password</Form.Label>
              <Form.Control
                type="password"
                name="accountConfirmPassword"
                value={form.accountConfirmPassword}
                onChange={handleChange}
                required
              />
            </Form.Group>

            {error && (
              <Alert variant="danger" dismissible onClose={() => setError("")}>
                {error}
              </Alert>
            )}

            <Button
              variant="primary"
              type="submit"
              className="w-100"
              disabled={loading}
            >
              {loading ? "Registering..." : "Register"}
            </Button>
          </Form>
          <div className="text-center mt-3">
            <Link to="/login">Already have an account? Login here.</Link>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}

export default Register;
