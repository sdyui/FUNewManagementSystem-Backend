import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import { Routes, Route } from "react-router";
import Layout from "./components/Layout";
import About from "./pages/About";
import Contact from "./pages/Contact";
import NotFound from "./pages/NotFound";
import Login from "./components/Auth/Login";
import Register from "./components/Auth/Register";
import NewsList from "./components/NewsList";
import AccountList from "./components/AccountList";
import CategoryList from "./components/CategoryList";
import NewsDetail from "./pages/NewsDetail";
import Home from "./pages/Home";
import MyArticles from "./pages/MyArticles";
import ArticleForm from "./pages/ArticleForm";
import ProtectedRoute from "./components/Auth/ProtectedRoute";
import ReviewQueue from "./pages/ReviewQueue";
import ReviewArticle from "./pages/ReviewArticle";
import PublicNewsDetail from "./pages/PublicNewsDetail";
import AuditLogs from "./pages/AuditLogs";

function App() {
  return (
    <Routes>
      <Route path="login" element={<Login />} />
      <Route path="register" element={<Register />} />

      {/* Layout: Header luôn có, Sidebar chỉ hiện khi đã đăng nhập */}
      <Route path="/" element={<Layout />}>
        {/* Guest: xem được không cần đăng nhập */}
        <Route index element={<Home />} />
        <Route path="post/:id" element={<PublicNewsDetail />} />
        <Route path="news/:id" element={<NewsDetail />} />
        <Route path="about" element={<About />} />
        <Route path="contact" element={<Contact />} />

        {/* Authenticated user routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="news" element={<NewsList />} />
          <Route path="articles" element={<NewsList />} />
          <Route path="articles/:id" element={<NewsDetail />} />

          {/* Editor & Admin */}
          <Route element={<ProtectedRoute roles={["EDITOR", "ADMIN"]} />}>
            <Route path="review" element={<ReviewQueue />} />
            <Route path="review/:id" element={<ReviewArticle />} />
          </Route>

          {/* Author (Staff, Contributor, Admin) */}
          <Route
            element={
              <ProtectedRoute roles={["STAFF", "CONTRIBUTOR", "ADMIN"]} />
            }
          >
            <Route path="my-articles" element={<MyArticles />} />
            <Route path="my-articles/new" element={<ArticleForm />} />
            <Route path="my-articles/:id/edit" element={<ArticleForm />} />
          </Route>

          {/* Admin */}
          <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
            <Route path="users" element={<AccountList />} />
            <Route path="category" element={<CategoryList />} />
            <Route path="audit-logs" element={<AuditLogs />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default App;
