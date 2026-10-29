import React from "react";
import { Link, useNavigate } from "react-router";

export default function NotFound() {
  const navigate = useNavigate();
  return (
    <div className="container text-center py-5">
      <h1 className="display-1 fw-bold text-muted">404</h1>
      <h2 className="mb-3">Page Not Found</h2>
      <p className="lead text-muted mb-4">
        The page you are looking for does not exist or has been moved.
      </p>
      <button onClick={() => navigate(-1)} className="btn btn-primary">
        Back to Previous Page
      </button>
    </div>
  );
}
