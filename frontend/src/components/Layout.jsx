import { Outlet } from "react-router";
import Header from "./Header";
import Sidebar from "./Sidebars";

export default function Layout() {
 

  return (
    <div className="d-flex flex-column" style={{ minHeight: "100vh" }}>
      {/* Header + Nav */}
      <Header />

      {/* Sidebar + Main Content */}
      <div className="d-flex flex-grow-1">
        <Sidebar />
        <main className="flex-grow-1 p-4">
          <Outlet />
        </main>
      </div>

      {/* Footer */}
      <footer className="border-top py-3 text-center text-body-secondary small bg-white">
        © {new Date().getFullYear()} FU News Management System — SBA301 React Demo
      </footer>
    </div>
  );
}
