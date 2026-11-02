import React from "react";
import { NavLink } from "react-router";
import Nav from "react-bootstrap/Nav";
import { getRole } from "./Auth/auth";

// Menu theo role
const MENUS = {
  ADMIN: [
    { to: "/news", label: "Articles" },
    { to: "/category", label: "Category" },
    { to: "/review", label: "Review Queue" },
    { to: "/users", label: "Users" },
  ],
  EDITOR: [
    { to: "/review", label: "Review Queue" },
    { to: "/news", label: "Articles" },
  ],
  STAFF: [{ to: "/news", label: "Articles" }],
  CONTRIBUTOR: [{ to: "/news", label: "Articles" }],
};

function Sidebar() {
  const navItems = MENUS[getRole()] || [
    { to: "/news", label: "Articles" },
    { to: "/category", label: "Category" },
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
            className="rounded px-3 py-2"
          >
            {item.label}
          </Nav.Link>
        ))}
      </Nav>
    </aside>
  );
}

export default Sidebar;
