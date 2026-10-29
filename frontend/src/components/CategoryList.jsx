import { useState } from "react";
import { Container, Table, Button, Form, InputGroup } from "react-bootstrap";
import { Link } from "react-router";
import { categories } from "../shared/categories";

const categoryList = Array.isArray(categories) ? categories : [categories];

export default function ManageCategory() {
  const [searchTerm, setSearchTerm] = useState("");

  // Search category
  let filteredCategories = categoryList;

  if (searchTerm.trim() !== "") {
    const keyword = searchTerm.toLowerCase();

    filteredCategories = categoryList.filter(
      (item) =>
        item.CategoryName?.toLowerCase().includes(keyword) ||
        item.CategoryDesciption?.toLowerCase().includes(keyword),
    );
  }

  return (
    <Container className="mt-4">
      {/* Header */}
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <h2 className="mb-0">Manage Categories</h2>

        <div className="d-flex gap-2">
          {/* Search */}
          <InputGroup style={{ maxWidth: "400px" }}>
            <Form.Control
              placeholder="Tìm kiếm danh mục..."
              aria-label="Tìm kiếm danh mục"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />

            <Button variant="primary">Search</Button>
          </InputGroup>

          {/* Add Category */}
          <Button
            as={Link}
            to="/categories/add"
            variant="success"
            className="text-nowrap"
          >
            + Add Category
          </Button>
        </div>
      </div>

      {/* Category Table */}
      <Table striped bordered hover responsive>
        <thead className="table-dark">
          <tr>
            <th>#</th>
            <th>Category ID</th>
            <th>Category Name</th>
            <th>Description</th>
            <th>Parent Category</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {filteredCategories.length > 0 ? (
            filteredCategories.map((item, index) => {
              
              const parentCategory = categoryList.find(
                (category) => category.CategoryID === item.ParentCategoryID,
              );

              return (
                <tr key={item.CategoryID}>
                  <td>{index + 1}</td>

                  <td>{item.CategoryID}</td>

                  <td>
                    <strong>{item.CategoryName}</strong>
                  </td>

                  <td>{item.CategoryDesciption}</td>

                  <td>
                    {parentCategory
                      ? parentCategory.CategoryName
                      : "Root Category"}
                  </td>

                  <td>
                    {item.IsActive ? (
                      <span className="badge bg-success">Active</span>
                    ) : (
                      <span className="badge bg-secondary">Inactive</span>
                    )}
                  </td>

                  <td>
                    <div className="d-flex gap-2">
                      <Button
                        variant="warning"
                        size="sm"
                        className="flex-fill"
                        as={Link}
                        to={`/categories/edit/${item.CategoryID}`}
                      >
                        Edit
                      </Button>

                      <Button variant="danger" size="sm" className="flex-fill">
                        Delete
                      </Button>
                    </div>
                  </td>
                </tr>
              );
            })
          ) : (
            <tr>
              <td colSpan="7" className="text-center">
                Không tìm thấy danh mục nào.
              </td>
            </tr>
          )}
        </tbody>
      </Table>
    </Container>
  );
}
