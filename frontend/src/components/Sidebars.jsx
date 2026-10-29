import React from "react";
import { NavLink } from "react-router";
import Nav from "react-bootstrap/Nav";

function Sidebar() {
  const navItems = [
    { to: "/", icon: "", label: "Dashboard" },
    { to: "/category", icon: "", label: "Category" },
    { to: "/news", icon: "", label: "News" },
    { to: "/users", icon: "", label: "Users" },
    { to: "/settings", icon: "", label: "Settings" },
  ];

  return (
    <aside
      className="bg-light border-end d-flex flex-column p-3"
      style={{ width: "220px", minHeight: "100%" }}
    >
      <Nav className="flex-column gap-1">
        {navItems.map((item) => (
          <Nav.Link
            key={item.to}
            as={NavLink}
            to={item.to}
            end={item.to === "/"}
            className="rounded px-3 py-2"
          >
            <span className="me-2">{item.icon}</span>
            {item.label}
          </Nav.Link>
        ))}
      </Nav>
    </aside>
  );
}

export default Sidebar;
