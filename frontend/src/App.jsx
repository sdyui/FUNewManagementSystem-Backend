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

function App() {
  return (
    <Routes>
      <Route path='/' element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="users" element={<AccountList />} />
        <Route path='about' element={<About />} />
        <Route path='category' element={<CategoryList/>}/>
        <Route path='contact' element={<Contact />} />
        <Route path='news' element={<NewsList />} />
        <Route path="news/:id" element={<NewsDetail />} />
        <Route path='articles' element={<NewsList />} />
        <Route path="articles/:id" element={<NewsDetail />} />
      {/*customer*/ }
      <Route index element={<Home />} />
      <Route path="news/:id" element={<NewsDetail />} />
      <Route path="login" element={<Login />} />
      <Route path="register" element={<Register />} />

      {/*login user*/ }
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="about" element={<About />} />
          <Route path="contact" element={<Contact />} />
          <Route path="news" element={<NewsList />} />
          
          {/*editor*/ }
          <Route element={<ProtectedRoute roles={["EDITOR", "ADMIN"]} />}>
            <Route path="review" element={<ReviewQueue />} />
            <Route path="review/:id" element={<ReviewArticle />} />
          </Route>

          {/*author*/ }
          <Route element={<ProtectedRoute roles={["STAFF", "CONTRIBUTOR", "ADMIN"]} />}>
            <Route path="my-articles" element={<MyArticles />} />
            <Route path="my-articles/new" element={<ArticleForm />} />
            <Route path="my-articles/:id/edit" element={<ArticleForm />} />
          </Route>

          {/*admin*/ }
          <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
            <Route path="users" element={<AccountList />} />
            <Route path="category" element={<CategoryList />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default App;
