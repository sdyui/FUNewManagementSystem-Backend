import './App.css'
import "bootstrap/dist/css/bootstrap.min.css";
import { Routes, Route } from "react-router";
import Layout from './components/Layout'
import About from './pages/About'
import Contact from './pages/Contact'
import NotFound from './pages/NotFound'
import Login from './components/Auth/Login'
import Register from './components/Auth/Register'
import NewsList from './components/NewsList'
import AccountList from './components/AccountList'
import CategoryList from './components/CategoryList'
import NewsDetail from './pages/NewsDetail';
import Home from './pages/Home';

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
      </Route>
      <Route path='login' element={<Login />} />
      <Route path='register' element={<Register />} />
      <Route path='*' element={<NotFound />} />
    </Routes>
  )
}

export default App
