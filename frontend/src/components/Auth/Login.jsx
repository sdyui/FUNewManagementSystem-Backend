import { useState } from "react";
import { Link } from "react-router";
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import Card from "react-bootstrap/Card";
import InputGroup from "react-bootstrap/InputGroup";
import { useNavigate } from "react-router";
import axios from "axios";
import Alert from "react-bootstrap/Alert";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1/";
function Login() {
  const [validated, setValidated] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [accountName, setAccountName] = useState("");
  const [accountPassword, setAccountPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const handleSubmit = async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    setValidated(true); 
    if (form.checkValidity() === false) {
      return;
    }
    try{
      const res = await axios.post(`${API_BASE_URL}auth/login`, {
        accountName: accountName,
        accountPassword: accountPassword,
      });
      localStorage.setItem("user", JSON.stringify(res.data));
      navigate("/");
    }catch (error) {
      if(error.response && error.response.status === 401){
        setError("Invalid username or password.");
      }else{
        setError("An error occurred. Please try again later.");
      }
    }
  }
  return (
    <div
      className="d-flex justify-content-center align-items-center"
      style={{ minHeight: "80vh" }}
    >
      <Card
        className="shadow-sm border-0"
        style={{ width: "100%", maxWidth: "450px" }}
      >
        <Card.Body className="p-4 p-md-5">
          {/* Header */}
          <div className="text-center mb-4">

            <img
              src="/images/logo.png"
              alt="FUNews Logo"
              style={{ height: "60px", marginBottom: "16px" }}
            />
            <h3 className="fw-bold mb-1">Welcome Back</h3>
            <p className="text-muted">Sign in to your account</p>
          </div>
          {error && (
            <Alert variant="danger" onClose={() => setError("")} dismissible>
              {error}
            </Alert>
          )}

          {/* Form */}
          <Form noValidate validated={validated} onSubmit={handleSubmit}>
            <Form.Group className="mb-3" controlId="loginName">
              <Form.Label>Account Name</Form.Label>
              <InputGroup hasValidation>
                <InputGroup.Text>
                  <i className="bi bi-envelope">✉</i>
                </InputGroup.Text>
                <Form.Control
                  required
                  type="text"
                  placeholder="Enter your account name"
                  value={accountName}
                  onChange={(e) => setAccountName(e.target.value)}
                />
                <Form.Control.Feedback type="invalid">
                  Please enter a valid account name.
                </Form.Control.Feedback>
              </InputGroup>
            </Form.Group>

            <Form.Group className="mb-3" controlId="loginPassword">
              <Form.Label>Password</Form.Label>
              <InputGroup hasValidation>
                <InputGroup.Text>
                  <i className="bi bi-lock">🔒</i>
                </InputGroup.Text>
                <Form.Control
                  required
                  type={showPassword ? "text" : "password"}
                  placeholder="Enter your password"
                  value={accountPassword}
                  onChange={(e) => setAccountPassword(e.target.value)}
                />
                <Button
                  variant="outline-secondary"
                  onClick={() => setShowPassword(!showPassword)}
                  tabIndex={-1}
                >
                  {showPassword ? "🙈" : "👁"}
                </Button>
                <Form.Control.Feedback type="invalid">
                  Please enter your password.
                </Form.Control.Feedback>
              </InputGroup>
            </Form.Group>

            <div className="d-flex justify-content-between align-items-center mb-4">
              <Form.Check type="checkbox" id="rememberMe" label="Remember me" />
              <Link to="#" className="text-decoration-none small">
                Forgot password?
              </Link>
            </div>

            <Button
              type="submit"
              variant="primary"
              className="w-100 py-2 fw-semibold"
            >
              Sign In
            </Button>
          </Form>

          {/* Footer */}
          <div className="text-center mt-4">
            <span className="text-muted">Don't have an account? </span>
            <Link to="/register" className="text-decoration-none fw-semibold">
              Sign Up
            </Link>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}

export default Login;
