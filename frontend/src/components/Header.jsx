import React from "react";
import { Link, useNavigate } from "react-router";
import Nav from "react-bootstrap/Nav";

function Header() {
  const navigate = useNavigate();

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

          {/* Nav links moved up here */}
          <Nav className="gap-2 d-none d-md-flex fw-medium">
            <Nav.Link as={Link} to="/news">News</Nav.Link>
            <Nav.Link as={Link} to="/about">About</Nav.Link>
            <Nav.Link as={Link} to="/contact">Contact</Nav.Link>
          </Nav>
        </div>

        {/* Right side: Admin dropdown */}
        <div className="dropdown">
          <button
            className="btn btn-light dropdown-toggle d-flex align-items-center gap-2"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <div
              className="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center"
              style={{ width: "36px", height: "36px" }}
            >
              A
            </div>
            <span>Admin</span>
          </button>
          <ul className="dropdown-menu dropdown-menu-end">
            <li>
              <h6 className="dropdown-header">Admin Account</h6>
            </li>
            <li>
              <button className="dropdown-item">Profile</button>
            </li>
            <li>
              <button className="dropdown-item">Settings</button>
            </li>
            <li>
              <hr className="dropdown-divider" />
            </li>
            <li>
              <button className="dropdown-item text-danger" onClick={handleLogout}>
                Logout
              </button>
            </li>
          </ul>
        </div>
      </div>
    </header>
  );
}

export default Header;
