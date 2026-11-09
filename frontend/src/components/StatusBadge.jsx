import { Badge } from "react-bootstrap";

// eslint-disable-next-line react-refresh/only-export-components
export const STATUS = {
  DRAFT: { label: "Draft", bg: "secondary" },
  PENDING_REVIEW: { label: "Pending review", bg: "warning", text: "dark" },
  CHANGES_REQUESTED: { label: "Changes requested", bg: "danger" },
  SCHEDULED: { label: "Scheduled", bg: "info", text: "dark" },
  PUBLISHED: { label: "Published", bg: "success" },
  ARCHIVED: { label: "Archived", bg: "dark" },
};

// Tác giả chỉ được sửa / gửi duyệt ở 2 trạng thái này (giống backend)
// eslint-disable-next-line react-refresh/only-export-components
export const EDITABLE = ["DRAFT", "CHANGES_REQUESTED"];

export default function StatusBadge({ status }) {
  const s = STATUS[status] || { label: status, bg: "light", text: "dark" };
  return (
    <Badge bg={s.bg} text={s.text}>
      {s.label}
    </Badge>
  );
}
