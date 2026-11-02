import React from "react";
import { Link, useNavigate } from "react-router";
import Nav from "react-bootstrap/Nav";
import Button from "react-bootstrap/Button";

function Header() {
  const navigate = useNavigate();

  let currentUserName = "User";
  const userStr = localStorage.getItem("user");
  if (userStr) {
    try {
      const parsed = JSON.parse(userStr);
      if (parsed.accountName) {
        currentUserName = parsed.accountName;
      } else if (parsed.role) {
        currentUserName = parsed.role;
      }
    } catch {
      // ignore
    }
  }

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("accessToken");
    navigate("/login");
  };

  return (
    <header className="navbar bg-white border-bottom shadow-sm">
      <div className="container-fluid d-flex justify-content-between align-items-center">
        {/* Left side: Logo + Nav */}
        <div className="d-flex align-items-center gap-4">
          <Link to="/">
            <img
              src="/images/logo.png"
              alt="FUNews Management System"
              style={{
                height: "60px",
                width: "auto",
                objectFit: "contain",
              }}
            />
          </Link>

          <Nav className="gap-2 d-none d-md-flex fw-medium">
            <Nav.Link as={Link} to="/news">
              Articles
            </Nav.Link>
            <Nav.Link as={Link} to="/category">
              Category
            </Nav.Link>
            <Nav.Link as={Link} to="/about">
              About
            </Nav.Link>
            <Nav.Link as={Link} to="/contact">
              Contact
            </Nav.Link>
          </Nav>
        </div>

        {/* Right side: Account info + Logout button */}
        <div className="d-flex align-items-center gap-3">
          <span className="fw-semibold text-secondary">{currentUserName}</span>
          <Button variant="outline-danger" size="sm" onClick={handleLogout}>
            Logout
          </Button>
        </div>
      </div>
    </header>
  );
}

export default Header;
