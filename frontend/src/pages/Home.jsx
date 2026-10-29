import { Container, Button } from "react-bootstrap";
import { Link } from "react-router";

export default function Home() {
  return (
    <Container>
      <h1>Welcome to FU News</h1>

      <p className="mb-4">
        Browse the latest news from FU News Management System.
      </p>

      <Button as={Link} to="/news" size="lg" variant="primary">
        View News
      </Button>
    </Container>
  );
}
