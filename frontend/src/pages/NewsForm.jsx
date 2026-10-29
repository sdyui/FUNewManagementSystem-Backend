import { Modal, Button, Form, Row, Col } from "react-bootstrap";
function NewsModal({ show, onHide, mode = "add", news = null }) {
  const isEdit = mode === "edit";
  return (
    <Modal show={show} onHide={onHide} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title> {isEdit ? "Edit News" : "Add News"} </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form>
          {/* News ID + Category */}
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>News Article ID</Form.Label>
                <Form.Control
                  type="text"
                  defaultValue={isEdit ? news?.NewsArticleID : ""}
                  placeholder="Example: ART-001"
                  disabled={isEdit}
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Category</Form.Label>
                <Form.Select defaultValue={isEdit ? news?.CategoryID : "1"}>
                  <option value="1">Technology</option>
                  <option value="2">Business</option>
                  <option value="3">Education</option>
                  <option value="4">Sports</option>
                </Form.Select>
              </Form.Group>
            </Col>
          </Row>
          {/* Title */}
          <Form.Group className="mb-3">
            <Form.Label>News Title</Form.Label>
            <Form.Control
              type="text"
              defaultValue={isEdit ? news?.NewsTitle : ""}
              placeholder="Enter news title"
            />
          </Form.Group>
          {/* Headline */}
          <Form.Group className="mb-3">
            <Form.Label>Headline</Form.Label>
            <Form.Control
              type="text"
              defaultValue={isEdit ? news?.Headline : ""}
              placeholder="Enter headline"
            />
          </Form.Group>
          {/* Content */}
          <Form.Group className="mb-3">
            <Form.Label>News Content</Form.Label>
            <Form.Control
              as="textarea"
              rows={7}
              defaultValue={isEdit ? news?.NewsContent : ""}
              placeholder="Enter news content"
            />
          </Form.Group>
          {/* Source */}
          <Form.Group className="mb-3">
            <Form.Label>News Source</Form.Label>
            <Form.Control
              type="text"
              defaultValue={isEdit ? news?.NewsSource : ""}
              placeholder="Example: TechCrunch Vietnam"
            />
          </Form.Group>
          {/* Status */}
          <Form.Group className="mb-3">
            <Form.Label>Status</Form.Label>
            <Form.Select
              defaultValue={
                isEdit ? (news?.NewsStatus ? "true" : "false") : "true"
              }
            >
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </Form.Select>
          </Form.Group>
          {/* Created By / Updated By */}
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Created By</Form.Label>
                <Form.Control
                  type="number"
                  defaultValue={isEdit ? news?.CreatedByID : ""}
                  placeholder="Created By ID"
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Updated By</Form.Label>
                <Form.Control
                  type="number"
                  defaultValue={isEdit ? news?.UpdatedByID : ""}
                  placeholder="Updated By ID"
                />
              </Form.Group>
            </Col>
          </Row>
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Cancel
        </Button>
        <Button variant="primary" onClick={onHide}>
          {isEdit ? "Save Changes" : "Add News"}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
export default NewsModal;
