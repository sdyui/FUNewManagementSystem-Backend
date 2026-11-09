# TÀI LIỆU GIẢI THÍCH TUẦN TỰ TỪNG TẦNG (FRONTEND -> CONTROLLER -> DTO -> SERVICE -> REPOSITORY -> ENTITY -> DATABASE) CHO TẤT CẢ CÁC LUỒNG CATEGORY & ARTICLE

Tài liệu này mổ xẻ **tuần tự từng tầng một** cho toàn bộ **5 luồng của Category** và **5 luồng của Article** theo đúng thứ tự:
> **Bước 0 (Frontend gửi gì?) -> Tầng 1 (`Controller` & `Request DTO`) -> Tầng 2 (`Service` xử lý logic) -> Tầng 3 (`Repository` & `Entity` & `SQL Database`) -> Tầng 4 (`Response DTO` -> `Controller` -> `Frontend` vẽ lại màn hình)**

---

# PHẦN I: 5 LUỒNG CỦA CATEGORY MANAGEMENT

## Các file tham gia trong luồng Category:
1. **Frontend:** [`CategoryList.jsx`](frontend/src/components/CategoryList.jsx)
2. **Tầng Controller:** [`CategoryController.java`](src/main/java/com/funewmanagementsystem/controller/CategoryController.java)
3. **Tầng Request DTO:** [`CategoryRequest.java`](src/main/java/com/funewmanagementsystem/dto/request/CategoryRequest.java)
4. **Tầng Service:** [`CategoryServiceImpl.java`](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java)
5. **Tầng Repository:** [`CategoryRepository.java`](src/main/java/com/funewmanagementsystem/repository/CategoryRepository.java)
6. **Tầng Entity (Ánh xạ bảng DB):** [`Category.java`](src/main/java/com/funewmanagementsystem/entity/Category.java)
7. **Tầng Response DTO:** [`CategoryResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/CategoryResponse.java), [`PageResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/PageResponse.java)

---

## LUỒNG CATEGORY 1: MỞ TRANG CATEGORY (TẢI DANH SÁCH BAN ĐẦU & DROPDOWN ACTIVE)

### Bước 0: Xuất phát từ Frontend ([`CategoryList.jsx` dòng 95-98](frontend/src/components/CategoryList.jsx#L95-L98))
* Ngay khi mở trang `/category`, `useEffect` tự động gọi 2 hàm:
  1. `fetchCategories()` ([dòng 60-79](frontend/src/components/CategoryList.jsx#L60-L79)): Gửi HTTP Request `GET http://localhost:8080/api/v1/categories?size=50` (không có Body JSON, chỉ gửi tham số `?size=50` trên URL).
  2. `fetchActiveCategories()` ([dòng 82-93](frontend/src/components/CategoryList.jsx#L82-L93)): Gửi HTTP Request `GET http://localhost:8080/api/v1/categories/active`.

### Tầng 1: `Controller` tiếp nhận ([`CategoryController.java` dòng 39-53](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L39-L53))
* Với `GET /api/v1/categories?size=50`:
  * Chạy vào hàm `searchCategories` ([dòng 40-46](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L40-L46)).
  * Vì không truyền `name` hay `isActive` trên URL, nên `@RequestParam String name = null`, `Boolean isActive = null`, còn `Pageable pageable` nhận `size = 50, page = 0, sort = id ASC`.
  * Controller gọi xuống `categoryService.searchCategories(name, isActive, pageable)`.
* Với `GET /api/v1/categories/active`:
  * Chạy vào hàm `getAllActiveCategories` ([dòng 50-53](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L50-L53)), gọi xuống `categoryService.getAllActiveCategories()`.

### Tầng 2: `Service` xử lý logic ([`CategoryServiceImpl.java` dòng 99-128](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L99-L128))
* Trong `searchCategories` ([dòng 100-112](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L100-L112)):
  1. Kiểm tra cột sắp xếp (`id`) có nằm trong danh sách cho phép `SORTABLE_FIELDS` (`"id"`, `"categoryName"`, `"isActive"`) hay không ([dòng 101-105](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L101-L105)).
  2. Gọi `categoryRepository.search(blankToNull(name), isActive, pageable)` ([dòng 107-108](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L107-L108)).
* Trong `getAllActiveCategories` ([dòng 117-128](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L117-L128)):
  1. Gọi `categoryRepository.findByIsActiveTrue()` ([dòng 118](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L118)).

### Tầng 3: `Repository` & `Entity` & `Database MySQL`
* Tại [`CategoryRepository.java` (dòng 23-36)](src/main/java/com/funewmanagementsystem/repository/CategoryRepository.java#L23-L36), hàm `search` thực thi câu lệnh JPQL:
  ```sql
  SELECT c FROM Category c
  LEFT JOIN FETCH c.parentCategory
  WHERE (:name IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:isActive IS NULL OR c.isActive = :isActive)
  ```
* Hibernate dịch câu JPQL này sang SQL thật dưới MySQL (dựa theo bản đồ cột trong [`Category.java` dòng 10-33](src/main/java/com/funewmanagementsystem/entity/Category.java#L10-L33)):
  * Lấy các dòng từ bảng `Category`, nối (`LEFT JOIN`) với chính bảng `Category` qua cột `ParentCategoryID` để lấy luôn thông tin danh mục cha.
  * Mỗi dòng lấy lên từ MySQL được đúc thành 1 đối tượng **`Category` (Entity)**.

### Tầng 4: Chiều quay về (`Entity` -> `Response DTO` -> `Controller` -> `Frontend`)
1. **Đóng gói sang `CategoryResponse` ([`CategoryServiceImpl.java` dòng 148-157](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L148-L157)):**
   * Hàm `mapToResponse(Category category)` rút dữ liệu từ `Category` (Entity) sang đối tượng [`CategoryResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/CategoryResponse.java):
     * `id` <- `category.getId()`
     * `categoryName` <- `category.getCategoryName()`
     * `categoryDescription` <- `category.getCategoryDescription()`
     * `parentCategoryId` <- `category.getParentCategory().getId()` (nếu có cha)
     * `parentCategoryName` <- `category.getParentCategory().getCategoryName()` (nếu có cha)
     * `isActive` <- `category.getIsActive()`
2. **Bọc vào `PageResponse` ([`PageResponse.java` dòng 19-22](src/main/java/com/funewmanagementsystem/dto/respone/PageResponse.java#L19-L22)):**
   * Đóng gói danh sách `List<CategoryResponse>` vào trường `content` kèm thông tin phân trang (`page`, `size`, `totalElements`, `totalPages`, `last`).
3. **Trả về Frontend ([`CategoryList.jsx` dòng 71-73 & 256-297](frontend/src/components/CategoryList.jsx#L256-L297)):**
   * `Controller` trả về HTTP `200 OK` kèm JSON.
   * Frontend lấy `data.content` đưa vào `setCategories(data.content || [])` ([dòng 72](frontend/src/components/CategoryList.jsx#L72)) -> React tự động chạy vòng lặp `categories.map(...)` ([dòng 257](frontend/src/components/CategoryList.jsx#L257)) vẽ bảng lên màn hình.

---

## LUỒNG CATEGORY 2: TÌM KIẾM DANH MỤC THEO TÊN (GÕ TỪ KHÓA -> NHẤN "SEARCH")

### Bước 0: Xuất phát từ Frontend ([`CategoryList.jsx` dòng 212-230](frontend/src/components/CategoryList.jsx#L212-L230))
* Khi người dùng gõ chữ (ví dụ `"AI"`) vào ô `<Form.Control>` ([dòng 220-226](frontend/src/components/CategoryList.jsx#L220-L226)), sự kiện `onChange` lưu chữ `"AI"` vào biến state `searchName` ([dòng 25](frontend/src/components/CategoryList.jsx#L25)).
* Khi nhấn nút **`Search`** ([dòng 228-230](frontend/src/components/CategoryList.jsx#L228-L230)), `<Form onSubmit={...}>` gọi lại hàm `fetchCategories()` ([dòng 60-79](frontend/src/components/CategoryList.jsx#L60-L79)).
* Tại **[dòng 64-66](frontend/src/components/CategoryList.jsx#L64-L66)**, vì `searchName.trim() !== ""`, URL được nối thêm `&name=AI`:
  * Gửi HTTP Request: `GET http://localhost:8080/api/v1/categories?size=50&name=AI`.

### Tầng 1: `Controller` tiếp nhận ([`CategoryController.java` dòng 39-46](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L39-L46))
* `@RequestParam(required = false) String name` hứng giá trị `"AI"` từ URL và truyền xuống `categoryService.searchCategories("AI", null, pageable)`.

### Tầng 2: `Service` xử lý ([`CategoryServiceImpl.java` dòng 99-112 & 159-161](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L99-L112))
* Hàm `blankToNull("AI")` ([dòng 159-161](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L159-L161)) cắt khoảng trắng 2 đầu (`trim()`), giữ nguyên chuỗi `"AI"` và truyền xuống `categoryRepository.search("AI", null, pageable)`.

### Tầng 3: `Repository` & `Entity` & `Database MySQL` ([`CategoryRepository.java` dòng 23-36](src/main/java/com/funewmanagementsystem/repository/CategoryRepository.java#L23-L36))
* Lúc này `:name` là `"AI"` (khác `NULL`), nên điều kiện `WHERE` trong câu truy vấn kích hoạt:
  ```sql
  LOWER(c.categoryName) LIKE LOWER(CONCAT('%', 'AI', '%'))
  ```
* MySQL tìm tất cả các dòng trong bảng `Category` mà cột `CategoryName` có chứa chữ `"ai"` (không phân biệt hoa/thường), đúc thành các đối tượng `Category` (Entity).

### Tầng 4: Chiều quay về (`Entity` -> `CategoryResponse` -> `PageResponse` -> `Controller` -> `Frontend`)
* Giống Luồng 1: Chuyển từng `Category` (Entity) sang `CategoryResponse` -> bọc vào `PageResponse` -> trả JSON về Frontend -> `setCategories(data.content)` vẽ lại bảng chỉ còn các danh mục khớp từ khóa.

---

## LUỒNG CATEGORY 3: THÊM MỚI DANH MỤC (`+ ADD CATEGORY` -> ĐIỀN FORM -> `SAVE`)

### Bước 0: Xuất phát từ Frontend ([`CategoryList.jsx` dòng 101-109 & 123-143](frontend/src/components/CategoryList.jsx#L101-L143))
1. Nhấn nút **`+ Add Category`** ([dòng 231-233](frontend/src/components/CategoryList.jsx#L231-L233)) -> chạy `handleOpenCreate()` ([dòng 101-109](frontend/src/components/CategoryList.jsx#L101-L109)):
   * Đặt `editingId = null` (đánh dấu đang Tạo mới).
   * Xóa trắng 4 biến state `categoryName`, `categoryDescription`, `parentCategoryId`, `isActive = true`.
   * Đặt `showModal = true` để mở hộp thoại `<Modal>` ([dòng 301](frontend/src/components/CategoryList.jsx#L301)).
2. Người dùng gõ vào 4 ô trong `<Modal.Body>` ([dòng 308-359](frontend/src/components/CategoryList.jsx#L308-L359)) -> sự kiện `onChange` lưu từng ký tự vào 4 biến state ở [dòng 30-33](frontend/src/components/CategoryList.jsx#L30-L33).
3. Nhấn nút **`Save`** ([dòng 363-365](frontend/src/components/CategoryList.jsx#L363-L365)) -> chạy `handleSave(e)` ([dòng 123-168](frontend/src/components/CategoryList.jsx#L123-L168)):
   * Gom 4 biến state thành `requestBody` ([dòng 128-134](frontend/src/components/CategoryList.jsx#L128-L134)).
   * Vì `editingId === null` ([dòng 138](frontend/src/components/CategoryList.jsx#L138)), gửi HTTP Request:
     * **Địa chỉ & Phương thức:** `POST http://localhost:8080/api/v1/categories`
     * **Body JSON:**
       ```json
       {
         "categoryName": "Thể thao",
         "categoryDescription": "Tin tức bóng đá, quần vợt",
         "parentCategoryId": null,
         "isActive": true
       }
       ```

### Tầng 1: `Controller` & `Request DTO` ([`CategoryController.java` dòng 64-68](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L64-L68))
```java
@PostMapping
public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
    CategoryResponse response = categoryService.createCategory(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```
* `@RequestBody CategoryRequest request`: Đổ cục JSON từ Frontend vào đối tượng [`CategoryRequest.java` (dòng 12-24)](src/main/java/com/funewmanagementsystem/dto/request/CategoryRequest.java#L12-L24).
* `@Valid`: Kiểm tra `@NotBlank` và `@Size(max = 100)` trên `categoryName`, `@Size(max = 1000)` trên `categoryDescription`. Nếu vi phạm thì chặn ngay tại Controller. Hợp lệ thì gọi `categoryService.createCategory(request)`.

### Tầng 2: `Service` xử lý logic ([`CategoryServiceImpl.java` dòng 35-54](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L35-L54))
1. **Kiểm tra Luật trùng tên ([dòng 36-38](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L36-L38)):**
   * Gọi `categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName().trim())`.
   * Nếu trong bảng `Category` đã có tên `"Thể thao"` -> Ném lỗi `DuplicateResourceException("Category already exists")` (HTTP `409 Conflict`).
2. **Tạo đối tượng `Category` (Entity) mới ([dòng 41-44](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L41-L44)):**
   * Khởi tạo `Category category = new Category();` (lúc này `category.id` đang là `null`).
   * Gán `categoryName`, `categoryDescription`, `isActive` từ `CategoryRequest` sang `Category` (Entity).
3. **Tìm và gán Danh mục cha nếu có ([dòng 46-50](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L46-L50)):**
   * Nếu `request.getParentCategoryId() != null`, gọi `categoryRepository.findById(...)` lấy đối tượng `Category parent` từ DB lên và gán `category.setParentCategory(parent)`.

### Tầng 3: `Repository` & `Entity` & `Database MySQL` ([`CategoryServiceImpl.java` dòng 52](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L52))
* Gọi `Category saved = categoryRepository.save(category);`.
* Vì `category.getId()` đang là `null`, Hibernate biết đây là đối tượng mới và dựa vào [`Category.java` (Entity)](src/main/java/com/funewmanagementsystem/entity/Category.java) để sinh câu lệnh SQL:
  ```sql
  INSERT INTO Category (CategoryName, CategoryDescription, ParentCategoryID, IsActive)
  VALUES ('Thể thao', 'Tin tức bóng đá, quần vợt', NULL, 1);
  ```
* MySQL tự động tăng khóa chính (`AUTO_INCREMENT` do `@GeneratedValue(strategy = GenerationType.IDENTITY)` ở [`Category.java` dòng 17](src/main/java/com/funewmanagementsystem/entity/Category.java#L17)) và trả ID mới (ví dụ `id = 14`) gán ngược lại vào `saved.id`.

### Tầng 4: Chiều quay về (`Entity` -> `CategoryResponse` -> `Controller` -> `Frontend`)
* `Service` gọi `mapToResponse(saved)` ([dòng 53](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L53)) đóng gói thành `CategoryResponse`.
* `Controller` trả về HTTP `201 CREATED` ([dòng 67](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L67)).
* Frontend ([`CategoryList.jsx` dòng 154-162](frontend/src/components/CategoryList.jsx#L154-L162)) nhận `response.ok == true` -> Hiện thông báo xanh `"Created category successfully!"`, đóng Modal (`setShowModal(false)`) và gọi `fetchCategories()` + `fetchActiveCategories()` để tải lại bảng.

---

## LUỒNG CATEGORY 4: CHỈNH SỬA DANH MỤC (`EDIT` -> SỬA DỮ LIỆU -> `SAVE`)

### Bước 0: Xuất phát từ Frontend ([`CategoryList.jsx` dòng 112-120 & 144-150](frontend/src/components/CategoryList.jsx#L112-L150))
1. Nhấn nút **`Edit`** trên dòng danh mục số `5` ([dòng 271-277](frontend/src/components/CategoryList.jsx#L271-L277)) -> chạy `handleOpenEdit(cat)` ([dòng 112-120](frontend/src/components/CategoryList.jsx#L112-L120)):
   * Ghi nhớ `setEditingId(5)`.
   * Đổ dữ liệu cũ của `cat` vào 4 biến state `categoryName`, `categoryDescription`, `parentCategoryId`, `isActive` và mở Modal (`setShowModal(true)`).
2. Người dùng sửa dữ liệu rồi nhấn **`Save`** ([dòng 363-365](frontend/src/components/CategoryList.jsx#L363-L365)) -> chạy `handleSave(e)`:
   * Vì `editingId` đang là `5` (khác `null`), nhảy vào nhánh `else` ([dòng 144-150](frontend/src/components/CategoryList.jsx#L144-L150)) gửi HTTP Request:
     * **Địa chỉ & Phương thức:** `PUT http://localhost:8080/api/v1/categories/5`
     * **Body JSON:** `{ "categoryName": "...", "categoryDescription": "...", "parentCategoryId": 1, "isActive": true }`

### Tầng 1: `Controller` & `Request DTO` ([`CategoryController.java` dòng 72-77](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L72-L77))
* `@PathVariable Long id`: Lấy số `5` trên đường dẫn URL.
* `@Valid @RequestBody CategoryRequest request`: Đổ Body JSON vào [`CategoryRequest.java`](src/main/java/com/funewmanagementsystem/dto/request/CategoryRequest.java) và kiểm tra `@NotBlank`, `@Size`.
* Gọi `categoryService.updateCategory(5L, request)`.

### Tầng 2: `Service` xử lý logic ([`CategoryServiceImpl.java` dòng 58-86](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L58-L86))
1. **Lấy bản ghi cũ từ DB ([dòng 59-60](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L59-L60)):** Gọi `categoryRepository.findById(5L)`. Nếu không có -> ném `ResourceNotFoundException` (`404`).
2. **Kiểm tra Luật 1 — Trùng tên với danh mục khác ([dòng 63-65](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L63-L65)):** Gọi `existsByCategoryNameIgnoreCaseAndIdNot(name, 5L)` (kiểm tra xem các dòng có `id != 5` có ai dùng tên này chưa).
3. **Kiểm tra Luật 2 — Không tự làm cha chính mình ([dòng 68-70](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L68-L70)):** Nếu `request.getParentCategoryId().equals(id)` -> ném `InvalidRequestException("A category cannot be its own parent")`.
4. **Cập nhật giá trị vào đối tượng `Category` (Entity) ([dòng 71-82](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L71-L82)):** Gán `parentCategory`, `categoryName`, `categoryDescription`, `isActive` mới vào `category`.

### Tầng 3: `Repository` & `Entity` & `Database MySQL` ([`CategoryServiceImpl.java` dòng 84](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L84))
* Gọi `Category updated = categoryRepository.save(category);`.
* Vì `category.id` đã có sẵn là `5`, Hibernate sinh câu lệnh SQL `UPDATE`:
  ```sql
  UPDATE Category
  SET CategoryName = ?, CategoryDescription = ?, ParentCategoryID = ?, IsActive = ?
  WHERE CategoryID = 5;
  ```

### Tầng 4: Chiều quay về (`Entity` -> `CategoryResponse` -> `Controller` -> `Frontend`)
* Chuyển `updated` (Entity) sang `CategoryResponse` qua `mapToResponse(updated)` ([dòng 85](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L85)) -> `Controller` trả về HTTP `200 OK` -> Frontend hiện thông báo `"Updated category successfully!"`, đóng Modal và tải lại bảng.

---

## LUỒNG CATEGORY 5: XÓA DANH MỤC (`DELETE`)

### Bước 0: Xuất phát từ Frontend ([`CategoryList.jsx` dòng 171-201 & 278-284](frontend/src/components/CategoryList.jsx#L171-L201))
* Nhấn nút **`Delete`** màu đỏ trên dòng có `id = 5` ([dòng 278-284](frontend/src/components/CategoryList.jsx#L278-L284)) -> chạy `handleDelete(5)`.
* Hiện hộp thoại xác nhận `window.confirm(...)`. Khi bấm OK, gửi HTTP Request:
  * **Địa chỉ & Phương thức:** `DELETE http://localhost:8080/api/v1/categories/5` (Không có Request Body).

### Tầng 1: `Controller` tiếp nhận ([`CategoryController.java` dòng 81-85](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L81-L85))
* `@DeleteMapping("/{id}")` lấy `id = 5` từ URL, gọi `categoryService.deleteCategory(5L)`.

### Tầng 2: `Service` kiểm tra điều kiện xóa ([`CategoryServiceImpl.java` dòng 133-146](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L133-L146))
1. **Kiểm tra tồn tại ([dòng 134-136](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L134-L136)):** Gọi `categoryRepository.existsById(5L)`. Không có -> báo lỗi `404 Not Found`.
2. **Kiểm tra ràng buộc danh mục con ([dòng 139-141](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L139-L141)):**
   * Gọi `categoryRepository.existsByParentCategory_Id(5L)`.
   * Nếu có bất kỳ danh mục nào đang trỏ `ParentCategoryID = 5` -> Chặn xóa và ném lỗi `InvalidRequestException("Cannot delete this category because it has sub-categories!")`.

### Tầng 3: `Repository` & `Entity` & `Database MySQL` ([`CategoryServiceImpl.java` dòng 144-145](src/main/java/com/funewmanagementsystem/service/impl/CategoryServiceImpl.java#L144-L145))
* Nếu không vướng danh mục con -> Gọi `categoryRepository.deleteById(5L)` và `categoryRepository.flush()`.
* Hibernate thực thi câu lệnh SQL dưới MySQL:
  ```sql
  DELETE FROM Category WHERE CategoryID = 5;
  ```

### Tầng 4: Chiều quay về (`Controller` -> `Frontend`)
* Vì xóa xong dữ liệu không còn nữa nên không cần `Response DTO`. `Controller` trả về `ResponseEntity.noContent().build()` (mã HTTP `204 No Content` ở [dòng 84](src/main/java/com/funewmanagementsystem/controller/CategoryController.java#L84)).
* Frontend ([`CategoryList.jsx` dòng 188-192](frontend/src/components/CategoryList.jsx#L188-L192)) kiểm tra `if (response.ok)` (mã `204` vẫn thuộc nhóm thành công `2xx` nên `response.ok == true`) -> Hiện thông báo `"Deleted category successfully!"` và gọi `fetchCategories()` tải lại bảng.

---
---

# PHẦN II: 5 LUỒNG CỦA ARTICLE MANAGEMENT (CONTRIBUTOR)

## Các file tham gia trong luồng Article:
1. **Frontend:** [`NewsList.jsx`](frontend/src/components/NewsList.jsx)
2. **Tầng Controller:** [`ArticleController.java`](src/main/java/com/funewmanagementsystem/controller/ArticleController.java)
3. **Tầng Request DTO:** [`CreateArticleRequest.java`](src/main/java/com/funewmanagementsystem/dto/request/CreateArticleRequest.java), [`UpdateArticleRequest.java`](src/main/java/com/funewmanagementsystem/dto/request/UpdateArticleRequest.java)
4. **Tầng Service:** [`ArticleServiceImpl.java`](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java), `ArticleWorkFlowServiceImpl.java`
5. **Tầng Repository:** [`NewsArticleRepository.java`](src/main/java/com/funewmanagementsystem/repository/NewsArticleRepository.java), `ArticleWorkflowLogRepository.java`
6. **Tầng Entity (Ánh xạ bảng DB):** [`NewsArticle.java`](src/main/java/com/funewmanagementsystem/entity/NewsArticle.java), `ArticleWorkflowLog.java`
7. **Tầng Response DTO:** [`ArticleDetailResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/ArticleDetailResponse.java), [`SubmitArticleResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/SubmitArticleResponse.java), [`PageResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/PageResponse.java)

---

## LUỒNG ARTICLE 1: MỞ TRANG ARTICLE & LỌC BÀI VIẾT CỦA TÔI (`GET /api/v1/articles/my`)

### Bước 0: Xuất phát từ Frontend ([`NewsList.jsx` dòng 67-86 & 103-106](frontend/src/components/NewsList.jsx#L67-L106))
* Khi vừa mở trang `/news` ([dòng 103-106](frontend/src/components/NewsList.jsx#L103-L106)) hoặc khi chọn trạng thái (ví dụ `DRAFT`) ở ô `<Form.Select>` ([dòng 254-265](frontend/src/components/NewsList.jsx#L254-L265)) rồi bấm nút **`Filter`** ([dòng 268-270](frontend/src/components/NewsList.jsx#L268-L270)):
* Hàm `fetchArticles()` ([dòng 67-86](frontend/src/components/NewsList.jsx#L67-L86)) gửi HTTP Request:
  * **Địa chỉ & Phương thức:** `GET http://localhost:8080/api/v1/articles/my?size=50&status=DRAFT`
  * **Header:** `Authorization: Bearer <token>`

### Tầng 1: `Controller` tiếp nhận ([`ArticleController.java` dòng 45-51](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L45-L51))
```java
@GetMapping("/my")
public ResponseEntity<PageResponse<ArticleDetailResponse>> getMyArticles(
        @RequestParam(required = false) ArticleStatus status,
        @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
    PageResponse<ArticleDetailResponse> response = articleService.getMyArticles(status, pageable);
    return ResponseEntity.ok(response);
}
```
* Spring tự động ép chuỗi `"DRAFT"` trên URL thành kiểu Enum `ArticleStatus.DRAFT` và truyền xuống `articleService.getMyArticles(status, pageable)`.

### Tầng 2: `Service` xử lý logic ([`ArticleServiceImpl.java` dòng 180-200](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L180-L200))
1. **Xác định ai đang đăng nhập ([dòng 181](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L181)):**
   * Gọi `SystemAccount currentAuthor = currentUserService.getCurrentAccount();` (lấy thông tin tài khoản từ JWT Token).
2. **Gọi xuống Repository ([dòng 183](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L183)):**
   * Truyền `currentAuthor.getId()`, `status` và `pageable` vào `newsArticleRepository.findMyArticles(...)`.

### Tầng 3: `Repository` & `Entity` & `Database MySQL` ([`NewsArticleRepository.java` dòng 26-36](src/main/java/com/funewmanagementsystem/repository/NewsArticleRepository.java#L26-L36))
* Hàm `findMyArticles` thực thi câu truy vấn JPQL:
  ```sql
  SELECT a FROM NewsArticle a
  WHERE a.createdBy.id = :authorId
    AND (:status IS NULL OR a.articleStatus = :status)
  ORDER BY a.createdDate DESC
  ```
* Dựa vào bản đồ ánh xạ trong [`NewsArticle.java` (Entity dòng 13-80)](src/main/java/com/funewmanagementsystem/entity/NewsArticle.java#L13-L80), Hibernate truy vấn bảng `NewsArticle` trong MySQL với điều kiện `CreatedByID = ? AND ArticleStatus = 'DRAFT'`, trả về danh sách các đối tượng **`NewsArticle` (Entity)**.

### Tầng 4: Chiều quay về (`Entity` -> `ArticleDetailResponse` -> `PageResponse` -> `Controller` -> `Frontend`)
1. **Chuyển đổi từ `NewsArticle` (Entity) sang [`ArticleDetailResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/ArticleDetailResponse.java) ([`ArticleServiceImpl.java` dòng 186-190 & 260-300](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L260-L300)):**
   * Vòng lặp `for` duyệt từng `NewsArticle` và gọi hàm `mapToDetailResponse(article)` để rút trích dữ liệu sang hộp `ArticleDetailResponse`:
     * `id`, `title`, `thumbnail`, `content`, `status`, `createdAt`, `updatedAt`, `publishDate`
     * `categoryId` & `categoryName` (rút từ `article.getCategory()`)
     * `author` (rút `id`, `name`, `email` từ `article.getCreatedBy()`, tuyệt đối **không** lấy cột mật khẩu!)
     * `latestFeedback` (nếu bài bị yêu cầu sửa, gọi `findLatestFeedback` lấy lời nhắn mới nhất của Editor từ bảng `ArticleWorkflowLog`).
2. **Bọc vào `PageResponse` ([dòng 192-199](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L192-L199))** và trả về qua `Controller` (`200 OK`).
3. **Frontend nhận và vẽ bảng ([`NewsList.jsx` dòng 79 & 295-360](frontend/src/components/NewsList.jsx#L295-L360)):**
   * Gọi `setArticles(data.content || [])` -> Vẽ từng dòng bài viết lên bảng `<Table>`.

---

## LUỒNG ARTICLE 2: TẠO BÀI VIẾT MỚI (`+ ADD ARTICLE` -> ĐIỀN FORM -> `SAVE`)

### Bước 0: Xuất phát từ Frontend ([`NewsList.jsx` dòng 109-117 & 131-150](frontend/src/components/NewsList.jsx#L109-L150))
1. Nhấn nút **`+ Add Article`** ([dòng 271-273](frontend/src/components/NewsList.jsx#L271-L273)) -> chạy `handleOpenCreate()`: đặt `editingId = null`, xóa trắng `title`, `thumbnail`, `content`, `categoryId` ([dòng 32-35](frontend/src/components/NewsList.jsx#L32-L35)), bật `showModal = true`.
2. Người dùng gõ dữ liệu vào 4 ô trong `<Modal.Body>` ([dòng 372-417](frontend/src/components/NewsList.jsx#L372-L417)) -> `onChange` lưu vào 4 biến state `title`, `categoryId`, `thumbnail`, `content`.
3. Nhấn nút **`Save`** ([dòng 423-425](frontend/src/components/NewsList.jsx#L423-L425)) -> chạy `handleSave(e)`:
   * Vì `editingId === null` ([dòng 145](frontend/src/components/NewsList.jsx#L145)), gửi HTTP Request:
     * **Địa chỉ & Phương thức:** `POST http://localhost:8080/api/v1/articles`
     * **Body JSON:**
       ```json
       {
         "title": "Bản tin công nghệ mới",
         "thumbnail": "https://example.com/img.png",
         "content": "Nội dung chi tiết bài báo...",
         "categoryId": 1
       }
       ```

### Tầng 1: `Controller` & `Request DTO` ([`ArticleController.java` dòng 36-40](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L36-L40))
```java
@PostMapping
public ResponseEntity<ArticleDetailResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
    ArticleDetailResponse response = articleService.createArticle(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```
* `@RequestBody CreateArticleRequest request`: Đổ JSON vào đối tượng [`CreateArticleRequest.java` (dòng 14-27)](src/main/java/com/funewmanagementsystem/dto/request/CreateArticleRequest.java#L14-L27).
* `@Valid`: Kiểm tra `title` không được trống (`@NotBlank`) và không quá 200 ký tự (`@Size(max = 200)`), `thumbnail` không quá 500 ký tự. Hợp lệ thì gọi `articleService.createArticle(request)`.

### Tầng 2: `Service` xử lý logic ([`ArticleServiceImpl.java` dòng 47-93](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L47-L93))
1. **Lấy tác giả đang đăng nhập ([dòng 48](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L48)):** `SystemAccount currentAuthor = currentUserService.getCurrentAccount();`.
2. **Khởi tạo `NewsArticle` (Entity) và áp dụng Quy tắc 1 & 2 ([dòng 50-60](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L50-L60)):**
   * Tạo `NewsArticle article = new NewsArticle();`
   * Gán `newsTitle`, `newsContent`, `thumbnail` từ `CreateArticleRequest`.
   * **Quy tắc 1:** Luôn ép trạng thái ban đầu là `article.setArticleStatus(ArticleStatus.DRAFT)` và `article.setNewsStatus(false)`.
   * **Quy tắc 2:** Gán tác giả `article.setCreatedBy(currentAuthor)` và ngày tạo `article.setCreatedDate(LocalDateTime.now())`.
3. **Tìm và gán đối tượng `Category` (Entity) ([dòng 63-80](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L63-L80)):**
   * Nếu `request.getCategoryId() != null`: gọi `categoryRepository.findById(...)` lấy đối tượng `Category` trong DB gán vào `article.setCategory(...)`.
   * Nếu người dùng không chọn Category (`null`): dùng vòng lặp `for` duyệt `categoryRepository.findAll()` lấy Category đang `isActive == true` đầu tiên làm mặc định.

### Tầng 3: `Repository` & `Entity` & `Database MySQL` ([`ArticleServiceImpl.java` dòng 91](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L91))
* Gọi `NewsArticle savedArticle = newsArticleRepository.save(article);`.
* Dựa vào bản đồ cột trong [`NewsArticle.java` (dòng 13-80)](src/main/java/com/funewmanagementsystem/entity/NewsArticle.java#L13-L80), Hibernate sinh câu lệnh SQL:
  ```sql
  INSERT INTO NewsArticle (NewsTitle, NewsContent, ThumbNail, ArticleStatus, NewsStatus, CreatedByID, CreatedDate, CategoryID, version)
  VALUES ('Bản tin công nghệ mới', 'Nội dung...', 'https://...', 'DRAFT', 0, 1, '2026-11-02 14:00:00', 1, 0);
  ```

### Tầng 4: Chiều quay về (`Entity` -> `ArticleDetailResponse` -> `Controller` -> `Frontend`)
* `Service` gọi `mapToDetailResponse(savedArticle)` ([dòng 92](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L92)) đổi sang `ArticleDetailResponse`.
* `Controller` trả về HTTP `201 CREATED`.
* Frontend ([`NewsList.jsx` dòng 161-169](frontend/src/components/NewsList.jsx#L161-L169)) hiện thông báo xanh `"Created article (DRAFT) successfully!"`, đóng Modal và gọi `fetchArticles()` tải lại bảng.

---

## LUỒNG ARTICLE 3: CHỈNH SỬA BÀI VIẾT (`EDIT` -> SỬA DỮ LIỆU -> `SAVE`)

### Bước 0: Xuất phát từ Frontend ([`NewsList.jsx` dòng 120-128 & 151-157](frontend/src/components/NewsList.jsx#L120-L157))
1. Nhấn nút **`Edit`** trên dòng bài viết có `id = 10` ([dòng 327-338](frontend/src/components/NewsList.jsx#L327-L338)) -> chạy `handleOpenEdit(art)`:
   * Ghi nhớ `setEditingId(10)`, đổ dữ liệu cũ vào 4 biến state `title`, `thumbnail`, `content`, `categoryId` và mở Modal (`setShowModal(true)`).
2. Sửa xong nhấn **`Save`** ([dòng 423-425](frontend/src/components/NewsList.jsx#L423-L425)) -> trong `handleSave(e)`, vì `editingId` bằng `10` (khác `null`), nhảy vào nhánh `else` ([dòng 151-157](frontend/src/components/NewsList.jsx#L151-L157)) gửi HTTP Request:
   * **Địa chỉ & Phương thức:** `PUT http://localhost:8080/api/v1/articles/10`
   * **Body JSON:** `{ "title": "...", "thumbnail": "...", "content": "...", "categoryId": 2 }`

### Tầng 1: `Controller` & `Request DTO` ([`ArticleController.java` dòng 65-71](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L65-L71))
* `@PathVariable Long id`: Lấy `id = 10` trên URL.
* `@Valid @RequestBody UpdateArticleRequest request`: Đổ JSON vào đối tượng [`UpdateArticleRequest.java` (dòng 15-28)](src/main/java/com/funewmanagementsystem/dto/request/UpdateArticleRequest.java#L15-L28) và kiểm tra `@NotBlank`, `@Size`.
* Gọi `articleService.updateArticle(10L, request)`.

### Tầng 2: `Service` kiểm tra Quy tắc 3 & 4 ([`ArticleServiceImpl.java` dòng 102-148](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L102-L148))
1. **Lấy bài viết cũ từ DB ([dòng 105-109](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L105-L109)):** Gọi `newsArticleRepository.findById(10L)`.
2. **Kiểm tra Quy tắc 3 — Đúng tác giả ([dòng 112-114](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L112-L114)):**
   * So sánh `article.getCreatedBy().getId()` với `currentAuthor.getId()`. Nếu không khớp -> ném `ForbiddenException` (`403 Forbidden`).
3. **Kiểm tra Quy tắc 4 — Đúng trạng thái cho phép sửa ([dòng 117-120](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L117-L120)):**
   * Kiểm tra `currentStatus != ArticleStatus.DRAFT && currentStatus != ArticleStatus.CHANGES_REQUESTED`. Nếu bài đang ở `PENDING_REVIEW` hoặc `PUBLISHED` -> ném `InvalidRequestException` (`400 Bad Request`).
4. **Ghi đè thông tin mới lên `NewsArticle` (Entity) ([dòng 123-135](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L123-L135)):**
   * Cập nhật `newsTitle`, `newsContent`, `thumbnail`, `modifiedDate = LocalDateTime.now()`, `updatedBy = currentAuthor`, và `category`.

### Tầng 3: `Repository` & `Entity` & `Database MySQL` ([`ArticleServiceImpl.java` dòng 146](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L146))
* Gọi `NewsArticle updatedArticle = newsArticleRepository.save(article);`.
* Hibernate sinh câu lệnh SQL `UPDATE`:
  ```sql
  UPDATE NewsArticle
  SET NewsTitle = ?, NewsContent = ?, ThumbNail = ?, CategoryID = ?, ModifiedDate = ?, UpdatedByID = ?, version = version + 1
  WHERE NewsArticleID = 10 AND version = ?;
  ```

### Tầng 4: Chiều quay về (`Entity` -> `ArticleDetailResponse` -> `Controller` -> `Frontend`)
* Chuyển `updatedArticle` sang `ArticleDetailResponse` qua `mapToDetailResponse` -> `Controller` trả về HTTP `200 OK` -> Frontend hiện thông báo `"Updated article successfully!"`, đóng Modal và tải lại bảng.

---

## LUỒNG ARTICLE 4: XEM CHI TIẾT BÀI VIẾT (`VIEW` -> `GET /api/v1/articles/{id}`)

### Bước 0: Xuất phát từ Frontend ([`NewsList.jsx` dòng 178-194 & 319-326](frontend/src/components/NewsList.jsx#L178-L194))
* Nhấn nút **`View`** màu xanh nhạt trên dòng bài viết số `10` ([dòng 319-326](frontend/src/components/NewsList.jsx#L319-L326)) -> chạy `handleViewDetail(10)`.
* Gửi HTTP Request: `GET http://localhost:8080/api/v1/articles/10`.

### Tầng 1: `Controller` tiếp nhận ([`ArticleController.java` dòng 56-60](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L56-L60))
* `@GetMapping("/{id}")` lấy `id = 10`, gọi `articleService.getArticleDetail(10L)`.

### Tầng 2 & Tầng 3: `Service` & `Repository` & `Entity` ([`ArticleServiceImpl.java` dòng 155-174](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L155-L174))
1. Gọi `newsArticleRepository.findById(10L)` lấy đối tượng `NewsArticle` (Entity) từ bảng `NewsArticle` trong MySQL lên.
2. **Kiểm tra Quy tắc 5 — Quyền xem chi tiết ([dòng 164-171](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L164-L171)):**
   * Kiểm tra người đang đăng nhập có phải tác giả bài viết (`isAuthor`) hoặc là `ADMIN` / `EDITOR` / `STAFF` (`isStaffOrAdmin`) hay không. Nếu là một Contributor khác cố xem bài của người khác -> ném `ForbiddenException` (`403`).

### Tầng 4: Chiều quay về (`Entity` -> `ArticleDetailResponse` -> `Controller` -> `Frontend`)
* `mapToDetailResponse(article)` đóng gói toàn bộ thông tin chi tiết vào [`ArticleDetailResponse.java`](src/main/java/com/funewmanagementsystem/dto/respone/ArticleDetailResponse.java) -> `Controller` trả về HTTP `200 OK`.
* Frontend ([`NewsList.jsx` dòng 186-187](frontend/src/components/NewsList.jsx#L186-L187)) lưu cục JSON vào biến state `selectedArticle` (`setSelectedArticle(data)`) và bật `setShowDetailModal(true)` -> Hộp thoại `<Modal show={showDetailModal}>` ở **[dòng 431-476](frontend/src/components/NewsList.jsx#L431-L476)** hiện lên toàn bộ nội dung bài báo.

---

## LUỒNG ARTICLE 5: GỬI BÀI VIẾT ĐỂ XÉT DUYỆT (`SUBMIT` -> `PUT /api/v1/articles/{id}/submit`)

### Bước 0: Xuất phát từ Frontend ([`NewsList.jsx` dòng 197-224 & 339-349](frontend/src/components/NewsList.jsx#L197-L224))
* Nhấn nút **`Submit`** màu xanh đậm trên dòng bài viết số `10` ([dòng 339-349](frontend/src/components/NewsList.jsx#L339-L349)) -> chạy `handleSubmitReview(10)`.
* Hiện bảng hỏi `window.confirm("Are you sure you want to submit this article for review?")`.
* Khi bấm OK, gửi HTTP Request:
  * **Địa chỉ & Phương thức:** `PUT http://localhost:8080/api/v1/articles/10/submit` (Không cần Request Body vì chỉ cần biết ID bài viết muốn nộp duyệt).

### Tầng 1: `Controller` tiếp nhận ([`ArticleController.java` dòng 76-80](src/main/java/com/funewmanagementsystem/controller/ArticleController.java#L76-L80))
```java
@PutMapping("/{id}/submit")
public ResponseEntity<SubmitArticleResponse> submitArticle(@PathVariable Long id) {
    SubmitArticleResponse response = articleService.submitForReview(id);
    return ResponseEntity.ok(response);
}
```
* Lấy `id = 10` từ URL và gọi `articleService.submitForReview(10L)`.

### Tầng 2: `Service` kiểm tra Quy tắc 6, 7 & 8 ([`ArticleServiceImpl.java` dòng 208-245](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L208-L245))
1. **Tìm bài viết trong DB ([dòng 211-215](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L211-L215)):** `newsArticleRepository.findById(10L)`.
2. **Kiểm tra đúng tác giả ([dòng 217-219](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L217-L219)):** Chỉ người tạo bài mới được nộp duyệt.
3. **Kiểm tra Quy tắc 8 — Tiêu đề và Nội dung không được rỗng ([dòng 221-226](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L221-L226)):**
   * Kiểm tra `article.getNewsTitle()` và `article.getNewsContent()` có bị `null` hoặc rỗng (`trim().isEmpty()`) không. Nếu rỗng -> chặn lại ngay với lỗi `InvalidRequestException("Cannot submit for review: content must not be empty")`.
4. **Kiểm tra Quy tắc 6 — Trạng thái hợp lệ ([dòng 228-231](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L228-L231)):**
   * Bài viết hiện tại bắt buộc phải đang ở `DRAFT` hoặc `CHANGES_REQUESTED`.
5. **Thực thi Quy tắc 7 — Chuyển trạng thái qua `ArticleWorkFlowService` ([dòng 233-238](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L233-L238)):**
   * Gọi `articleWorkFlowService.transition(article, ArticleStatus.PENDING_REVIEW, currentAuthor, "Submitted for review by author")`.

### Tầng 3: `Repository` & `Entity` & `Database MySQL`
Tại `articleWorkFlowService.transition(...)`, 2 bảng trong MySQL được cập nhật cùng lúc trong 1 giao dịch (`@Transactional`):
1. **Bảng 1 (`NewsArticle`):** Đổi `article.setArticleStatus(ArticleStatus.PENDING_REVIEW)` -> Chạy lệnh SQL:
   ```sql
   UPDATE NewsArticle SET ArticleStatus = 'PENDING_REVIEW', ModifiedDate = ?, version = version + 1 WHERE NewsArticleID = 10;
   ```
2. **Bảng 2 (`ArticleWorkflowLog`):** Tạo 1 đối tượng `ArticleWorkflowLog` (Entity) mới ghi lại lịch sử (`fromStatus = DRAFT`, `toStatus = PENDING_REVIEW`, `actor = currentAuthor`, `note = 'Submitted for review by author'`) -> Chạy lệnh SQL:
   ```sql
   INSERT INTO ArticleWorkflowLog (NewsArticleID, FromStatus, ToStatus, ActorID, Note, ActionAt) VALUES (10, 'DRAFT', 'PENDING_REVIEW', ...);
   ```

### Tầng 4: Chiều quay về (`SubmitArticleResponse` -> `Controller` -> `Frontend`)
1. **Đóng gói vào [`SubmitArticleResponse.java` (dòng 12-20)](src/main/java/com/funewmanagementsystem/dto/respone/SubmitArticleResponse.java#L12-L20) ([`ArticleServiceImpl.java` dòng 240-244](src/main/java/com/funewmanagementsystem/service/impl/ArticleServiceImpl.java#L240-L244)):**
   ```json
   {
     "article_id": 10,
     "new_status": "PENDING_REVIEW",
     "message": "Article submitted for review successfully"
   }
   ```
2. `Controller` trả về HTTP `200 OK`.
3. **Frontend cập nhật màn hình ([`NewsList.jsx` dòng 215-220](frontend/src/components/NewsList.jsx#L215-L220)):**
   * Hiện thanh thông báo xanh `data.message` (`"Article submitted for review successfully"`).
   * Gọi `fetchArticles()` tải lại bảng -> Dòng bài viết số `10` đổi nhãn sang `<Badge bg="warning">PENDING_REVIEW</Badge>` màu vàng, đồng thời 2 nút `Edit` và `Submit` tự động khóa mờ đi!
