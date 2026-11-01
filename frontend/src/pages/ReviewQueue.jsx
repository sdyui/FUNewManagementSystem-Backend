import { useEffect, useState } from "react";
import api, { getErrorMessage } from "../api/axiosClient";
import { Link } from "react-router";

import { Table, Button, Alert, Spinner } from "react-bootstrap";
const PAGE_SIZE = 10;
const formatDate = (d) => (d ? new Date(d).toLocaleString() : "-");
export default function ReviewQueue() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState({
    content: [],
    totalPages: 0,
    totalElements: 0,
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get("review/pending", {
          params: { page, size: PAGE_SIZE, sort: "modifiedDate,asc" },
        });
        setData(res.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [page]);
  return (
    <div>
      <h3 className="mb-1">Review queue</h3>
      <p className="text-muted mb-3">
        {data.totalElements} article(s) waiting for review
      </p>

      {error && <Alert variant="danger">{error}</Alert>}

      {loading ? (
        <div className="text-center py-5">
          <Spinner animation="border" />
        </div>
      ) : (
        <Table hover responsive className="align-middle">
          <thead className="table-light">
            <tr>
              <th>#</th>
              <th>Title</th>
              <th>Author</th>
              <th>Category</th>
              <th>Submitted</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data.content.length === 0 ? (
              <tr>
                <td colSpan="6" className="text-center text-muted py-4">
                  Nothing to review 🎉
                </td>
              </tr>
            ) : (
              data.content.map((a, i) => (
                <tr key={a.id}>
                  <td>{page * PAGE_SIZE + i + 1}</td>
                  <td>
                    <strong>{a.title}</strong>
                  </td>
                  <td>{a.author?.name}</td>
                  <td>{a.categoryName || "—"}</td>
                  <td className="small">{formatDate(a.updated_at)}</td>
                  <td>
                    <Button as={Link} to={`/review/${a.id}`} size="sm">
                      Review
                    </Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      )}

      {data.totalPages > 1 && (
        <div className="d-flex justify-content-center align-items-center gap-3">
          <Button
            variant="outline-secondary"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            ← Prev
          </Button>
          <span>
            Page {page + 1} / {data.totalPages}
          </span>
          <Button
            variant="outline-secondary"
            size="sm"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next →
          </Button>
        </div>
      )}
    </div>
  );
}
