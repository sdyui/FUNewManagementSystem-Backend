export function getUser() {
  try {
    return JSON.parse(localStorage.getItem("user"));
  } catch {
    return null;
  }
}
export function getRole() {
  return getUser()?.account?.accountRole ?? null;
}
export const HOME_BY_ROLE = {
  ADMIN: "/my-articles",
  EDITOR: "/review",
  STAFF: "/my-articles",
  CONTRIBUTOR: "/my-articles",
};
