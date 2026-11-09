import { Link, useNavigate } from "react-router";
import Nav from "react-bootstrap/Nav";
import { getUser } from "./Auth/auth";

function Header() {
  const account = getUser()?.account;
  const initial = account?.accountName?.charAt(0).toUpperCase() || "?";
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("accessToken");
    void navigate("/login");
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
              style={{ height: "45px" }}
            />
          </Link>
        </div>

        {/* Right side: User Profile */}
        <div className="d-flex align-items-center gap-3">
          <div className="text-end">
            <h6 className="mb-0 fw-semibold">
              {account?.accountName || "Guest"}
            </h6>
            <span className="badge bg-primary text-uppercase">
              {account?.accountRole || "Guest"}
            </span>
          </div>

          <div className="dropdown">
            <div
              className="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center"
              style={{
                width: "42px",
                height: "42px",
                fontSize: "18px",
                cursor: "pointer",
              }}
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              {initial}
            </div>
            <ul className="dropdown-menu dropdown-menu-end shadow">
              <li>
                <Nav.Link
                  as={Link}
                  to="/profile"
                  className="dropdown-item px-3 py-2"
                >
                  My Profile
                </Nav.Link>
              </li>
              <li>
                <Nav.Link
                  as={Link}
                  to="/settings"
                  className="dropdown-item px-3 py-2"
                >
                  Settings
                </Nav.Link>
              </li>
              <li>
                <hr className="dropdown-divider" />
              </li>
              <li>
                <button
                  type="button"
                  className="dropdown-item px-3 py-2 text-danger"
                  onClick={handleLogout}
                >
                  Logout
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </header>
  );
}

export default Header;
