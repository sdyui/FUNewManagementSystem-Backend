import { Outlet } from "react-router";
import Header from "./Header";
import Sidebar from "./Sidebars";
import { getUser } from "./Auth/auth";

export default function Layout() {
  const loggedIn = !!getUser();

  return (
    <div className="d-flex flex-column" style={{ minHeight: "100vh" }}>
      <Header />

      <div className="d-flex flex-grow-1">
        {loggedIn && <Sidebar />}
        <main className="flex-grow-1 p-4">
          <Outlet />
        </main>
      </div>

      <footer className="border-top py-3 text-center text-body-secondary small bg-white">
        © {new Date().getFullYear()} FU News Management System — SBA301 React
        Demo
      </footer>
    </div>
  );
}
