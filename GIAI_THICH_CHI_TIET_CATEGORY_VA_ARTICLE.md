# TÀI LIỆU GIẢI THÍCH CHI TIẾT LUỒNG HOẠT ĐỘNG: CATEGORY & ARTICLE (FE -> BE -> DATABASE)

Tài liệu này giải thích chi tiết từng dòng code, từng nút bấm trên **Frontend (ReactJS + React-Bootstrap)** gửi request xuống **Backend (Spring Boot)** và truy vấn **Database (MySQL)** cho 2 chức năng:
1. **Quản lý Danh mục (Category Management)**
2. **Quản lý Bài viết của Tác giả (Contributor Article Management)**

---

## PHẦN 1: LUỒNG XÁC THỰC & GỬI TOKEN (DÙNG CHUNG CHO CẢ CATEGORY VÀ ARTICLE)

Trước khi sử dụng Category hoặc Article, người dùng phải đăng nhập tại [`Login.jsx`](frontend/src/components/Auth/Login.jsx):
1. Khi nhấn nút **Sign In** ([`Login.jsx` dòng 126-132](frontend/src/components/Auth/Login.jsx#L126-L132)), hàm `handleSubmit` ([dòng 21-45](frontend/src/components/Auth/Login.jsx#L21-L45)) gọi API `POST http://localhost:8080/api/v1/auth/login` gửi `{ accountName, accountPassword }`.
2. Backend kiểm tra tài khoản trong bảng `SystemAccount` và trả về `accessToken` (chuỗi JWT).
3. Frontend lưu thông tin user và `accessToken` vào bộ nhớ trình duyệt `localStorage` ([`Login.jsx` dòng 33-36](frontend/src/components/Auth/Login.jsx#L33-L36)) rồi chuyển hướng về trang chủ `/`.
4. Ở cả 2 file [`CategoryList.jsx` (dòng 38-57)](frontend/src/components/CategoryList.jsx#L38-L57) và [`NewsList.jsx` (dòng 46-64)](frontend/src/components/NewsList.jsx#L46-L64), hàm `getHeaders()` sẽ đọc `accessToken` từ `localStorage` và gắn vào Header của mọi request HTTP:
   ```js
   headers["Authorization"] = `Bearer ${token}`;
   ```
5. Khi request tới Backend, chốt kiểm tra [`SercurityConfig.java` (dòng 27-43)](src/main/java/com/funewmanagementsystem/config/SercurityConfig.java#L27-L43) và `JwtAuthentificationFilter` sẽ giải mã Token này để biết ai đang đăng nhập và có quyền (`ADMIN`, `STAFF`, `CONTRIBUTOR`) hay không.

---

## PHẦN 2: LUỒNG QUẢN LÝ DANH MỤC (CATEGORY MANAGEMENT)

### Các file tham gia:
* **Frontend:** [`frontend/src/components/CategoryList.jsx`](frontend/src/components/CategoryList.jsx)
* **Controller (BE):** [`CategoryController.java`](src/main/java/com/funewmanagementsystem/controller/CategoryController.java)
* **Service (BE):** [`CategoryServiceImpl.java`](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java)
* **Repository (BE):** [`CategoryRepository.java`](src/main/java/com/funewmanagementsystem/repository/CategoryRepository.java)
* **Entity & DTO (BE):** [`Category.java`](src/main/java/com/funewmanagementsystem/entity/Category.java), [`CategoryRequestDTO.java`](src/main/java/com/funewmanagementsystem/dto/request/CategoryRequestDTO.java), [`CategoryResponseDTO.java`](src/main/java/com/funewmanagementsystem/dto/respone/CategoryResponseDTO.java)

---

### 2.1. Các biến State trong `CategoryList.jsx` ([dòng 18-35](frontend/src/components/CategoryList.jsx#L18-L35))
* `categories` ([dòng 20](frontend/src/components/CategoryList.jsx#L20)): Mảng chứa danh sách Category để vẽ ra bảng `<Table>`.
* `activeCategories` ([dòng 22](frontend/src/components/CategoryList.jsx#L22)): Mảng chứa các Category đang hoạt động (`isActive = true`) để đổ vào ô chọn **Parent Category** (`<Form.Select>`).
* `searchName` ([dòng 25](frontend/src/components/CategoryList.jsx#L25)): Lưu từ khóa người dùng gõ vào ô tìm kiếm.
* `showModal` ([dòng 28](frontend/src/components/CategoryList.jsx#L28)): `true` thì mở hộp thoại (Modal) Thêm/Sửa, `false` thì đóng.
* `editingId` ([dòng 29](frontend/src/components/CategoryList.jsx#L29)):
  * Nếu `editingId === null`: Đang ở chế độ **Tạo mới (Add Category)**.
  * Nếu `editingId` có giá trị (ví dụ `3`): Đang ở chế độ **Chỉnh sửa (Edit Category)** có ID = `3`.
* `categoryName`, `categoryDescription`, `parentCategoryId`, `isActive` ([dòng 30-33](frontend/src/components/CategoryList.jsx#L30-L33)): Lưu giá trị của 4 ô nhập trong Modal.

---

### 2.2. Kịch bản 1: Vừa mở trang Category (Tải danh sách ban đầu)
1. **Frontend tự chạy `useEffect` ([`CategoryList.jsx` dòng 95-98](frontend/src/components/CategoryList.jsx#L95-L98)):**
   * Gọi `fetchCategories()` ([dòng 60-78](frontend/src/components/CategoryList.jsx#L60-L78)) -> gửi `GET http://localhost:8080/api/v1/categories?size=50`.
   * Gọi `fetchActiveCategories()` ([dòng 81-93](frontend/src/components/CategoryList.jsx#L81-L93)) -> gửi `GET http://localhost:8080/api/v1/categories/active`.
2. **Backend xử lý:**
   * Vào [`CategoryController.getAllCategories` (dòng 41-49)](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L41-L49).
   * Gọi [`CategoryServiceImpl.getAllCategories` (dòng 96-108)](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L96-L108).
   * Gọi [`CategoryRepository.searchCategories` (dòng 19-26)](src/main/java/com/funewmanagementsystem/repository/CategoryRepository.java#L19-L26) chạy câu lệnh JPQL lấy dữ liệu từ bảng `Category` trong MySQL.
   * Chuyển từng `Category` thành `CategoryResponseDTO` qua hàm `mapToDTO` ([`CategoryServiceImpl.java` dòng 159-169](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L159-L169)) và bọc vào `PageResponse`.
3. **Frontend nhận dữ liệu và vẽ bảng:**
   * Tại [`CategoryList.jsx` dòng 71](frontend/src/components/CategoryList.jsx#L71): `setCategories(data.content || [])`.
   * Vòng lặp `categories.map((cat, index) => ...)` ([dòng 262-302](frontend/src/components/CategoryList.jsx#L262-L302)) vẽ từng dòng `<tr>` lên bảng.

---

### 2.3. Kịch bản 2: Tìm kiếm Category theo tên
1. **Người dùng gõ chữ vào ô tìm kiếm ([`CategoryList.jsx` dòng 226-232](frontend/src/components/CategoryList.jsx#L226-L232)):**
   * Sự kiện `onChange={(e) => setSearchName(e.target.value)}` cập nhật biến `searchName`.
2. **Người dùng nhấn nút `Search` ([`CategoryList.jsx` dòng 234-236](frontend/src/components/CategoryList.jsx#L234-L236)):**
   * Kích hoạt `<Form onSubmit={...}>` ([dòng 218-222](frontend/src/components/CategoryList.jsx#L218-L222)) gọi lại hàm `fetchCategories()`.
   * Tại [dòng 63-65](frontend/src/components/CategoryList.jsx#L63-L65), vì `searchName` khác rỗng nên URL được nối thêm `&name=...`.
3. **Backend lọc dữ liệu:**
   * [`CategoryRepository.searchCategories` (dòng 19-26)](src/main/java/com/funewmanagementsystem/repository/CategoryRepository.java#L19-L26) thực thi điều kiện `LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%'))` và trả về các danh mục khớp tên.

---

### 2.4. Kịch bản 3: Thêm mới Category (`+ Add Category` -> `Save`)
1. **Nhấn nút `+ Add Category` màu xanh lá ([`CategoryList.jsx` dòng 237-239](frontend/src/components/CategoryList.jsx#L237-L239)):**
   * Chạy hàm `handleOpenCreate()` ([dòng 101-109](frontend/src/components/CategoryList.jsx#L101-L109)): đặt `setEditingId(null)`, xóa trắng các ô nhập liệu và mở Modal (`setShowModal(true)`).
2. **Điền thông tin vào Modal ([`CategoryList.jsx` dòng 312-363](frontend/src/components/CategoryList.jsx#L312-L363)) và nhấn nút `Save` ([dòng 368-370](frontend/src/components/CategoryList.jsx#L368-L370)):**
   * Kích hoạt hàm `handleSave(e)` ([dòng 123-168](frontend/src/components/CategoryList.jsx#L123-L168)).
   * Đóng gói `requestBody` ([dòng 128-134](frontend/src/components/CategoryList.jsx#L128-L134)).
   * Kiểm tra `if (editingId === null)` ([dòng 138](frontend/src/components/CategoryList.jsx#L138)) -> Đúng -> Gửi `POST http://localhost:8080/api/v1/categories` ([dòng 139-143](frontend/src/components/CategoryList.jsx#L139-L143)).
3. **Backend xử lý tạo mới:**
   * Vào [`CategoryController.createCategory` (dòng 64-70)](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L64-L70) kiểm tra `@Valid` (tên không được trống).
   * Gọi [`CategoryServiceImpl.createCategory` (dòng 35-55)](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L35-L55):
     * Nếu có chọn `parentCategoryId`, kiểm tra danh mục cha có tồn tại trong DB không ([dòng 37-41](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L37-L41)).
     * Khởi tạo `Category` mới, gán `isActive = true` (mặc định nếu không truyền), lưu vào MySQL bằng `categoryRepository.save(category)` ([dòng 53](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L53)).
4. **Frontend cập nhật giao diện ([`CategoryList.jsx` dòng 153-162](frontend/src/components/CategoryList.jsx#L153-L162)):**
   * Hiện thông báo xanh `"Created category successfully!"`, đóng Modal (`setShowModal(false)`), và gọi `fetchCategories()` + `fetchActiveCategories()` để làm mới bảng.

---

### 2.5. Kịch bản 4: Chỉnh sửa Category (`Edit` -> `Save`)
1. **Nhấn nút `Edit` màu vàng trên dòng bảng ([`CategoryList.jsx` dòng 277-283](frontend/src/components/CategoryList.jsx#L277-L283)):**
   * Chạy hàm `handleOpenEdit(cat)` ([dòng 112-120](frontend/src/components/CategoryList.jsx#L112-L120)): gán `setEditingId(cat.id)`, đổ dữ liệu hiện tại của `cat` vào form và mở Modal.
2. **Sửa thông tin và nhấn `Save` ([dòng 368-370](frontend/src/components/CategoryList.jsx#L368-L370)):**
   * Trong `handleSave(e)`, vì `editingId !== null`, code nhảy vào nhánh `else` ([dòng 144-150](frontend/src/components/CategoryList.jsx#L144-L150)) gửi `PUT http://localhost:8080/api/v1/categories/${editingId}`.
3. **Backend kiểm tra điều kiện sửa ([`CategoryServiceImpl.updateCategory` dòng 58-92](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L58-L92)):**
   * Không cho phép chọn chính nó làm danh mục cha (`id.equals(requestDTO.getParentCategoryId())` ở [dòng 66-68](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L66-L68)).
   * Nếu đổi trạng thái sang `Inactive` (`isActive = false`), kiểm tra `existsByCategoryId(id)` ([dòng 78-84](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L78-L84)) — nếu danh mục đang có bài báo thì chặn lại và báo lỗi `400 Bad Request`.
   * Hợp lệ thì gọi `categoryRepository.save(category)` ([dòng 90](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L90)) cập nhật xuống MySQL.

---

### 2.6. Kịch bản 5: Xóa Category (`Delete`)
1. **Nhấn nút `Delete` màu đỏ trên dòng bảng ([`CategoryList.jsx` dòng 284-290](frontend/src/components/CategoryList.jsx#L284-L290)):**
   * Chạy hàm `handleDelete(cat.id)` ([dòng 171-196](frontend/src/components/CategoryList.jsx#L171-L196)): bật `window.confirm(...)`, nếu bấm OK thì gửi `DELETE http://localhost:8080/api/v1/categories/${id}`.
2. **Backend kiểm tra ràng buộc trước khi xóa ([`CategoryServiceImpl.deleteCategory` dòng 133-155](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L133-L155)):**
   * Kiểm tra `existsByCategoryId(id)` ([dòng 139-143](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L139-L143)): nếu có bài báo thuộc danh mục này -> chặn xóa, báo lỗi.
   * Kiểm tra `existsByParentCategoryId(id)` ([dòng 146-151](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L146-L151)): nếu đang có danh mục con -> chặn xóa, báo lỗi.
   * Nếu thỏa mãn cả hai -> gọi `categoryRepository.delete(category)` ([dòng 154](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L154)) xóa khỏi MySQL.

---

## PHẦN 3: LUỒNG QUẢN LÝ BÀI VIẾT (CONTRIBUTOR ARTICLE MANAGEMENT)

### Các file tham gia:
* **Frontend:** [`frontend/src/components/NewsList.jsx`](frontend/src/components/NewsList.jsx)
* **Controller (BE):** [`ArticleController.java`](src/main/java/com/funewmanagementsystem/controller/ArticleController.java)
* **Service (BE):** [`ArticleServiceImpl.java`](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java)
* **Repository (BE):** [`NewsArticleRepository.java`](src/main/java/com/funewmanagementsystem/repository/NewsArticleRepository.java)
* **Entity & DTO (BE):** [`NewsArticle.java`](src/main/java/com/funewmanagementsystem/entity/NewsArticle.java), [`CreateArticleRequest.java`](src/main/java/com/funewmanagementsystem/dto/request/CreateArticleRequest.java), [`UpdateArticleRequest.java`](src/main/java/com/funewmanagementsystem/dto/request/UpdateArticleRequest.java), [`ArticleDetailResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/ArticleDetailResponse.java), [`SubmitArticleResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/SubmitArticleResponse.java)

---

### 3.1. Các biến State trong `NewsList.jsx` ([dòng 21-43](frontend/src/components/NewsList.jsx#L21-L43))
* `articles` ([dòng 22](frontend/src/components/NewsList.jsx#L22)): Mảng chứa danh sách bài viết của chính tác giả đang đăng nhập để vẽ ra bảng `<Table>`.
* `activeCategories` ([dòng 24](frontend/src/components/NewsList.jsx#L24)): Danh sách các Category đang hoạt động để người dùng chọn trong Modal Tạo/Sửa bài viết.
* `filterStatus` ([dòng 27](frontend/src/components/NewsList.jsx#L27)): Trạng thái bài viết cần lọc (`""`, `"DRAFT"`, `"PENDING_REVIEW"`, `"CHANGES_REQUESTED"`, `"APPROVED"`, `"PUBLISHED"`, `"REJECTED"`).
* `showModal`, `editingId`, `title`, `thumbnail`, `content`, `categoryId` ([dòng 30-35](frontend/src/components/NewsList.jsx#L30-L35)): Các state điều khiển hộp thoại Thêm/Sửa bài viết (tương tự Category: `editingId === null` là Tạo mới, khác `null` là Sửa).
* `showDetailModal`, `selectedArticle` ([dòng 38-39](frontend/src/components/NewsList.jsx#L38-L39)): Điều khiển hộp thoại Xem chi tiết bài viết khi nhấn nút `View`.
* `message`, `error` ([dòng 42-43](frontend/src/components/NewsList.jsx#L42-L43)): Hiển thị thanh thông báo `<Alert>` xanh hoặc đỏ.

---

### 3.2. Kịch bản 1: Mở trang Article & Lọc danh sách bài viết của tôi (`GET /api/v1/articles/my`)
1. **Tự động tải khi mở trang ([`NewsList.jsx` dòng 103-106](frontend/src/components/NewsList.jsx#L103-L106)) hoặc khi chọn Status và nhấn nút `Filter` ([dòng 254-270](frontend/src/components/NewsList.jsx#L254-L270)):**
   * Chạy hàm `fetchArticles()` ([dòng 67-86](frontend/src/components/NewsList.jsx#L67-L86)).
   * Tạo URL `http://localhost:8080/api/v1/articles/my?size=50`. Nếu người dùng chọn trạng thái ở ô `<Form.Select>` ([dòng 254-265](frontend/src/components/NewsList.jsx#L254-L265)), biến `filterStatus` có giá trị nên nối thêm `&status=...` ([dòng 71-73](frontend/src/components/NewsList.jsx#L71-L73)).
   * Gửi request `GET` kèm JWT Token trong `getHeaders()`.
2. **Backend xử lý lấy danh sách bài viết của tác giả đang đăng nhập:**
   * Request đi vào [`ArticleController.getMyArticles` (dòng 42-48)](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L42-L48).
   * Gọi [`ArticleServiceImpl.getMyArticles` (dòng 186-205)](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L186-L205):
     * **Bước 1 ([dòng 187](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L187)):** Gọi `currentUserService.getCurrentAccount()` để lấy thông tin tài khoản đang đăng nhập từ JWT Token.
     * **Bước 2 ([dòng 189](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L189)):** Gọi [`NewsArticleRepository.findMyArticles` (dòng 26-36)](src/main/java/com/funewmanagementsystem/repository/NewsArticleRepository.java#L26-L36) thực thi câu truy vấn JPQL:
       ```sql
       SELECT a FROM NewsArticle a
       WHERE a.createdBy.id = :authorId
       AND (:status IS NULL OR a.articleStatus = :status)
       ORDER BY a.createdDate DESC
       ```
       Câu truy vấn này đảm bảo **tác giả nào chỉ nhìn thấy bài viết của chính tác giả đó** (`a.createdBy.id = :authorId`).
     * **Bước 3 ([dòng 192-196](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L192-L196)):** Dùng vòng lặp `for` duyệt qua từng `NewsArticle`, gọi hàm `mapToDetailResponse(article)` để đổi sang `ArticleDetailResponse` rồi trả về `PageResponse`.
3. **Frontend nhận dữ liệu và hiển thị lên bảng ([`NewsList.jsx` dòng 79 & 282-361](frontend/src/components/NewsList.jsx#L282-L361)):**
   * Lưu vào `setArticles(data.content || [])`.
   * Vẽ bảng `<Table>` với `articles.map((art, index) => ...)`: hiển thị STT, ID, Title, Category, Author, `<Badge>` màu theo `art.status` ([dòng 306-316](frontend/src/components/NewsList.jsx#L306-L316)), và 3 nút hành động: `View`, `Edit`, `Submit`.
   * Đặc biệt tại **[dòng 331-334](frontend/src/components/NewsList.jsx#L331-L334)** và **[dòng 342-345](frontend/src/components/NewsList.jsx#L342-L345)**: thuộc tính `disabled={art.status !== "DRAFT" && art.status !== "CHANGES_REQUESTED"}` tự động làm mờ (khóa) nút `Edit` và `Submit` nếu bài viết đã gửi duyệt (`PENDING_REVIEW`) hoặc đã xuất bản (`PUBLISHED`).

---

### 3.3. Kịch bản 2: Tạo bài viết mới (`+ Add Article` -> `Save` -> `POST /api/v1/articles`)
1. **Người dùng nhấn nút `+ Add Article` màu xanh lá ([`NewsList.jsx` dòng 271-273](frontend/src/components/NewsList.jsx#L271-L273)):**
   * Kích hoạt hàm `handleOpenCreate()` ([dòng 109-117](frontend/src/components/NewsList.jsx#L109-L117)):
     * Đặt `setEditingId(null)` (đánh dấu đang tạo mới).
     * Xóa trắng `title`, `thumbnail`, `content`, `categoryId`.
     * Đặt `setShowModal(true)` để mở hộp thoại `<Modal>` tại [dòng 364](frontend/src/components/NewsList.jsx#L364).
2. **Người dùng nhập dữ liệu vào Modal ([`NewsList.jsx` dòng 372-417](frontend/src/components/NewsList.jsx#L372-L417)) và nhấn nút `Save` ([dòng 423-425](frontend/src/components/NewsList.jsx#L423-L425)):**
   * Kích hoạt `<Form onSubmit={handleSave}>` ([dòng 365](frontend/src/components/NewsList.jsx#L365)) -> chạy hàm `handleSave(e)` ([dòng 131-175](frontend/src/components/NewsList.jsx#L131-L175)).
   * Tạo object `requestBody` ([dòng 136-141](frontend/src/components/NewsList.jsx#L136-L141)):
     ```js
     const requestBody = {
       title: title,
       thumbnail: thumbnail,
       content: content,
       categoryId: categoryId === "" ? null : Number(categoryId),
     };
     ```
   * Kiểm tra `if (editingId === null)` ([dòng 145](frontend/src/components/NewsList.jsx#L145)) -> Đúng -> Gửi `POST http://localhost:8080/api/v1/articles` ([dòng 146-150](frontend/src/components/NewsList.jsx#L146-L150)).
3. **Backend xử lý tạo bài viết mới:**
   * Vào [`ArticleController.createArticle` (dòng 33-37)](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L33-L37): kiểm tra `@Valid` trên [`CreateArticleRequest.java`](src/main/java/com/funewmanagementsystem/dto/request/CreateArticleRequest.java) (`title` không được để trống và tối đa 200 ký tự, `thumbnail` tối đa 500 ký tự).
   * Gọi [`ArticleServiceImpl.createArticle` (dòng 49-101)](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L49-L101):
     * **Quy tắc 1 & 2 (Tác giả & Trạng thái mặc định):** Lấy tài khoản đang đăng nhập `currentAuthor = currentUserService.getCurrentAccount()` ([dòng 51](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L51)), gán `article.setArticleStatus(ArticleStatus.DRAFT)` ([dòng 59](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L59)), `article.setCreatedBy(currentAuthor)` ([dòng 62](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L62)), `article.setCreatedDate(LocalDateTime.now())` ([dòng 63](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L63)).
     * **Gán Danh mục ([dòng 66-83](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L66-L83)):** Nếu người dùng có chọn `categoryId` thì tìm trong DB và gán vào; nếu để trống thì dùng vòng lặp `for` tìm danh mục `isActive == true` đầu tiên để gán mặc định.
     * **Lưu xuống MySQL ([dòng 95](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L95)):** Gọi `newsArticleRepository.save(article)` thực hiện lệnh `INSERT INTO NewsArticle ...` và trả về mã `201 Created`.
4. **Frontend nhận kết quả ([`NewsList.jsx` dòng 161-169](frontend/src/components/NewsList.jsx#L161-L169)):**
   * Hiện thông báo xanh `"Created article (DRAFT) successfully!"`, đóng Modal (`setShowModal(false)`), và gọi `fetchArticles()` để hiển thị bài viết mới lên đầu bảng.

---

### 3.4. Kịch bản 3: Chỉnh sửa bài viết (`Edit` -> `Save` -> `PUT /api/v1/articles/{id}`)
1. **Người dùng nhấn nút `Edit` màu vàng trên dòng bài viết ([`NewsList.jsx` dòng 327-338](frontend/src/components/NewsList.jsx#L327-L338)):**
   * Chạy hàm `handleOpenEdit(art)` ([dòng 120-128](frontend/src/components/NewsList.jsx#L120-L128)):
     * Lưu ID bài viết đang sửa vào `setEditingId(art.id)`.
     * Đổ dữ liệu hiện tại (`art.title`, `art.thumbnail`, `art.content`, `art.categoryId`) vào các state của Form.
     * Mở Modal (`setShowModal(true)`), tiêu đề Modal lúc này hiện `"Edit Article"` ([dòng 368](frontend/src/components/NewsList.jsx#L368)).
2. **Người dùng sửa nội dung và nhấn nút `Save` ([dòng 423-425](frontend/src/components/NewsList.jsx#L423-L425)):**
   * Chạy hàm `handleSave(e)`. Vì `editingId` khác `null`, code chạy nhánh `else` ([dòng 151-157](frontend/src/components/NewsList.jsx#L151-L157)) gửi request `PUT http://localhost:8080/api/v1/articles/${editingId}` kèm `requestBody`.
3. **Backend kiểm tra quyền & trạng thái trước khi cập nhật:**
   * Vào [`ArticleController.updateArticle` (dòng 62-68)](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L62-L68) -> gọi [`ArticleServiceImpl.updateArticle` (dòng 108-155)](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L108-L155):
     * **Bước 1 ([dòng 112-116](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L112-L116)):** Tìm bài viết theo `id`, nếu không có ném `ResourceNotFoundException` (404).
     * **Bước 2 — Quy tắc 3 ([dòng 119-121](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L119-L121)):** Kiểm tra người đang đăng nhập có phải tác giả bài viết không (`!article.getCreatedBy().getId().equals(currentAuthor.getId())`). Nếu không phải -> ném `ForbiddenException` (403).
     * **Bước 3 — Quy tắc 4 ([dòng 124-127](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L124-L127)):** Kiểm tra trạng thái hiện tại chỉ được phép là `DRAFT` hoặc `CHANGES_REQUESTED`. Nếu đang ở `PENDING_REVIEW` hoặc `PUBLISHED` -> ném `InvalidRequestException` (400).
     * **Bước 4 ([dòng 130-153](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L130-L153)):** Cập nhật `newsTitle`, `newsContent`, `thumbnail`, `modifiedDate`, `updatedBy`, `category` và gọi `newsArticleRepository.save(article)` để `UPDATE` xuống MySQL.
4. **Frontend cập nhật lại bảng ([`NewsList.jsx` dòng 164-169](frontend/src/components/NewsList.jsx#L164-L169)):**
   * Hiện thông báo `"Updated article successfully!"`, đóng Modal và gọi `fetchArticles()`.

---

### 3.5. Kịch bản 4: Xem chi tiết bài viết (`View` -> `GET /api/v1/articles/{id}`)
1. **Người dùng nhấn nút `View` màu xanh nhạt trên dòng bài viết ([`NewsList.jsx` dòng 319-326](frontend/src/components/NewsList.jsx#L319-L326)):**
   * Chạy hàm `handleViewDetail(art.id)` ([dòng 178-194](frontend/src/components/NewsList.jsx#L178-L194)) gửi `GET http://localhost:8080/api/v1/articles/${id}`.
2. **Backend kiểm tra quyền xem chi tiết:**
   * Vào [`ArticleController.getArticleDetail` (dòng 53-57)](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L53-L57) -> gọi [`ArticleServiceImpl.getArticleDetail` (dòng 161-180)](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L161-L180):
     * Kiểm tra **Quy tắc 5**: Nếu là `CONTRIBUTOR` thì chỉ được xem bài viết do chính mình tạo (`isAuthor`), trừ khi có quyền `STAFF` / `EDITOR` / `ADMIN` ([dòng 170-177](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L170-L177)).
     * Trả về đầy đủ thông tin `ArticleDetailResponse`.
3. **Frontend mở Modal Chi tiết ([`NewsList.jsx` dòng 186-187 & 431-476](frontend/src/components/NewsList.jsx#L431-L476)):**
   * Gán `setSelectedArticle(data)` và `setShowDetailModal(true)` để bật hộp thoại hiển thị đầy đủ ID, Title, Status, Category, Author, Thumbnail và toàn bộ Content của bài viết.

---

### 3.6. Kịch bản 5: Gửi bài viết để xét duyệt (`Submit` -> `PUT /api/v1/articles/{id}/submit`)
1. **Người dùng nhấn nút `Submit` màu xanh đậm trên dòng bài viết ([`NewsList.jsx` dòng 339-349](frontend/src/components/NewsList.jsx#L339-L349)):**
   * Chạy hàm `handleSubmitReview(art.id)` ([dòng 197-224](frontend/src/components/NewsList.jsx#L197-L224)):
     * Hiện hộp thoại xác nhận `window.confirm("Are you sure you want to submit this article for review?")`.
     * Nếu nhấn OK, gửi request `PUT http://localhost:8080/api/v1/articles/${id}/submit` ([dòng 209-212](frontend/src/components/NewsList.jsx#L209-L212)).
2. **Backend kiểm tra các quy tắc nghiệp vụ và chuyển trạng thái:**
   * Vào [`ArticleController.submitArticle` (dòng 73-77)](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L73-L77) -> gọi [`ArticleServiceImpl.submitForReview` (dòng 213-252)](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L213-L252):
     * **Kiểm tra quyền tác giả ([dòng 223-225](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L223-L225)):** Chỉ tác giả tạo bài mới được phép gửi duyệt.
     * **Quy tắc 8 — Kiểm tra dữ liệu bắt buộc ([dòng 227-232](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L227-L232)):** Kiểm tra `newsTitle` và `newsContent` không được rỗng. Nếu bài nháp chưa viết nội dung (`content` trống) mà bấm Submit -> Backend chặn lại ngay và trả về lỗi `"Không thể gửi xét duyệt: Nội dung bài viết không được để trống!"`.
     * **Quy tắc 6 — Kiểm tra trạng thái hợp lệ ([dòng 234-237](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L234-L237)):** Bài viết hiện tại phải đang ở `DRAFT` hoặc `CHANGES_REQUESTED`.
     * **Quy tắc 7 — Chuyển trạng thái & Ghi Audit Log ([dòng 239-244](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L239-L244)):** Gọi `articleWorkFlowService.transition(article, ArticleStatus.PENDING_REVIEW, currentAuthor, ...)` để đổi cột `ArticleStatus` trong bảng `NewsArticle` sang `PENDING_REVIEW` đồng thời lưu lịch sử chuyển trạng thái vào bảng Audit Log.
     * Trả về `SubmitArticleResponse` gồm `{ id, status: "PENDING_REVIEW", message: "Bài viết đã được gửi xét duyệt thành công!" }`.
3. **Frontend cập nhật lại giao diện ([`NewsList.jsx` dòng 215-220](frontend/src/components/NewsList.jsx#L215-L220)):**
   * Nếu thành công: hiện thanh `<Alert variant="success">` với nội dung `"Bài viết đã được gửi xét duyệt thành công!"`, gọi `fetchArticles()` để tải lại bảng -> Lúc này dòng bài viết đổi sang nhãn `<Badge bg="warning">PENDING_REVIEW</Badge>` màu vàng và 2 nút `Edit` / `Submit` tự động chuyển sang trạng thái khóa (`disabled`).
   * Nếu lỗi (ví dụ bài nháp chưa có nội dung): hiện thanh `<Alert variant="danger">` màu đỏ báo rõ lý do từ Backend để người dùng bấm `Edit` bổ sung nội dung rồi mới `Submit` lại.

