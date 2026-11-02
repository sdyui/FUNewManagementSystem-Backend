import { Navigate, Outlet } from "react-router";
import { getUser, getRole } from "./auth";
export default function ProtectedRoute({ roles }) {
  if (!getUser()) {
    return <Navigate to="/" replace />;          
  }
  if (roles && !roles.includes(getRole())) {
    return <Navigate to="/" replace />;          
  }
  return <Outlet />;                             
}