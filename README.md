# Hệ thống quản lý sinh viên (QLSV)

Dự án **Spring Boot** quản lý danh sách sinh viên: có **REST API** đầy đủ, **giao diện admin** (HTML + JavaScript gọi API), lưu trữ **Microsoft SQL Server**, tài liệu API bằng **Swagger (springdoc)**. Có thể chạy trên máy bằng **Maven** hoặc đóng gói bằng **Docker Compose** gồm **SQL Server + ứng dụng + Nginx** (reverse proxy).

---

## Mô tả toàn bộ dự án

### Mục đích

Ứng dụng web phục vụ thao tác **thêm — sửa — xem danh sách — tìm kiếm — phân trang** thông tin sinh viên, kèm **upload ảnh đại diện**. Dữ liệu được lưu trong SQL Server; API thiết kế để có thể mở rộng client (web/mobile) khác ngoài giao diện admin hiện tại.

### Chức năng chính

| Nhóm | Nội dung |
|------|-----------|
| **Danh sách** | Phân trang, sắp xếp theo một số trường (MSSV, họ tên, ngày tạo/cập nhật, khóa, email, …). |
| **Tìm kiếm** | Tham số `keyword` trên API: khớp một phần MSSV, họ tên, email, SĐT, mã lớp, ngành, địa chỉ thường trú. |
| **CRUD** | Tạo mới, cập nhật, xem chi tiết qua REST và qua trang admin. |
| **Mã số sinh viên (MSSV)** | Định dạng tự động dạng `SV001`, `SV002`, … khi tạo mới; khi sửa không đổi MSSV (server quản lý/ghi đè theo quy tắc nghiệp vụ). |
| **Xóa mềm** | Đánh dấu không hiển thị trong danh sách hoạt động, bản ghi vẫn trong database. |
| **Upload ảnh** | Ảnh đại diện (JPG/PNG/WEBP, giới hạn kích thước trong cấu hình); file phục vụ qua URL `/uploads/...`. |
| **API & tài liệu** | REST tại tiền tố **`/api/students`**; Swagger UI và OpenAPI JSON. |

### Giao diện và luồng truy cập

- **Thymeleaf** dùng cho layout và trang tĩnh; trang **admin sinh viên** tại **`/admin/students`** dùng **fetch** gọi REST.
- **Trang chủ / điều hướng**: có controller điều hướng tới khu vực admin (`HomeController`).

### Công nghệ sử dụng

| Thành phần | Ghi chú |
|-------------|---------|
| **Java** | 21 (`pom.xml`) |
| **Spring Boot** | 3.5.x — Web, Thymeleaf, Validation, Data JPA |
| **SQL Server** | Driver `mssql-jdbc`; Hibernate `ddl-auto` (mặc định dev: `update`) |
| **springdoc-openapi** | 2.8.x — Swagger UI tương thích Spring Boot 3.5 |
| **Lombok** | Giảm boilerplate entity/DTO (tuỳ chỗ dùng) |
| **Apache POI** | Hỗ trợ xử lý tài liệu bảng tính (nếu module export/import dùng tới) |
| **Spring Actuator** | Health/readiness (phục vụ Docker và giám sát nhẹ) |

### Xử lý lỗi & cấu hình web

- **REST**: handler thống nhất (JSON lỗi, mã HTTP phù hợp).
- **CORS**: cho phép gọi **`/api/**`** từ nhiều nguồn (phù hợp dev; production nên thu hẹp `allowedOriginPatterns`).
- **Upload**: thư mục gốc cấu hình bằng `app.upload.base-dir` (local: thường `uploads`; Docker: volume `/data/uploads`).

### Cấu trúc mã nguồn (rút gọn)

```
src/main/java/com/example/studentManagements/
├── SchoolmanagerApplication.java    # Entry Spring Boot
├── HomeController.java              # / , /admin
├── config/                          # Web MVC, CORS, xử lý lỗi API
└── students/                        # Entity, repository, service, REST, upload, DTO, mapper

src/main/resources/
├── application.properties             # Chạy local
├── application-docker.properties      # Profile docker (Compose)
├── admin/students/index.html         # UI CRUD (gọi REST)
└── layout/                           # Thymeleaf layout, fragment header/sidebar/footer
```

---

## Chạy bằng Docker (Docker Compose + Nginx)

Không cần cài **JDK/Maven** trên máy để build image ứng dụng: **`Dockerfile`** dùng Maven trong container, sau đó chạy JAR bằng JRE.

### Tóm tắt nhanh (đã quen Docker)

```bash
cp .env.example .env          # chỉnh MSSQL_SA_PASSWORD
docker compose up -d sqlserver
# đợi SQL khởi động, tạo DB QLSV (script hoặc SSMS — xem bước chi tiết bên dưới)
docker compose up -d --build
```

Trình duyệt: **`http://localhost/`** (hoặc `http://localhost:<NGINX_HTTP_PORT>/` nếu bạn đổi cổng trong `.env`).

### Yêu cầu

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/macOS) hoặc Docker Engine + plugin **Compose** (`docker compose`).

### Bước 1 — Biến môi trường `.env`

Trong thư mục gốc dự án (có `docker-compose.yml`):

```bash
cp .env.example .env
```

Mở **`.env`**, đặt **`MSSQL_SA_PASSWORD`** đủ mạnh (Microsoft: độ dài, chữ hoa/thường, số, ký tự đặc biệt). File **`.env`** không commit (đã có trong `.gitignore`).

| Biến (tuỳ chọn) | Mặc định | Ý nghĩa |
|-----------------|----------|---------|
| `NGINX_HTTP_PORT` | `80` | Nếu cổng 80 bận → ví dụ `8088`, truy cập `http://localhost:8088/` |
| `SQLSERVER_HOST_PORT` | `14333` | SQL Server map ra máy host (tránh trùng instance SQL cài sẵn ở **1433**) |

### Bước 2 — Khởi động SQL Server và tạo database `QLSV`

```bash
docker compose up -d sqlserver
```

Đợi **khoảng 15–40 giây** (lần đầu có thể lâu hơn). Tạo database **`QLSV`** bằng một trong hai cách:

**Cách A — Script có sẵn trong repo**

```bash
docker compose cp docker/sql/init-qlsv.sql sqlserver:/tmp/init-qlsv.sql
docker compose exec sqlserver bash -c '/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -i /tmp/init-qlsv.sql'
```

Nếu báo không tìm thấy `sqlcmd`, thử đường dẫn: `/opt/mssql-tools/bin/sqlcmd` (cùng các tham số còn lại).

**Cách B — SSMS / Azure Data Studio**

Kết nối tới **`localhost,<SQLSERVER_HOST_PORT>`** (mặc định `localhost,14333`), user **`sa`**, mật khẩu trùng `.env`, chạy:

```sql
CREATE DATABASE QLSV;
```

### Bước 3 — Build image ứng dụng và chạy cả stack

```bash
docker compose up -d --build
```

- **`sqlserver`**: có healthcheck (TCP 1433 trong container).
- **`app`**: chỉ chạy sau khi SQL **healthy**; có kiểm tra **readiness** (gồm **DB**).
- **`nginx`**: chỉ chạy sau khi **app healthy**; có endpoint **`/healthz`** cho healthcheck.

### Bước 4 — Địa chỉ sau khi chạy

| URL | Mô tả |
|-----|--------|
| `http://localhost/` | Trang chủ / điều hướng (qua Nginx) |
| `http://localhost/admin/students` | Giao diện quản lý sinh viên |
| `http://localhost/swagger-ui/index.html` | Swagger UI |
| `http://localhost/v3/api-docs` | OpenAPI JSON |

Ứng dụng Spring Boot **không** publish cổng **8080** ra host; truy cập web qua **Nginx** (cổng cấu hình trong `.env`).

### Các lần chạy sau (lần 2, 3, …)

Nếu chỉ dùng `docker compose down` (**không** `-v`), volume dữ liệu **giữ nguyên** — không cần tạo lại `QLSV`:

```bash
docker compose up -d
```

Sau khi **sửa code Java**, `Dockerfile`, `pom.xml` hoặc `application-docker.properties`:

```bash
docker compose up -d --build
```

Nếu đã chạy **`docker compose down -v`**: volume bị xóa → mất DB và file upload trong volume; cần làm lại từ **Bước 2** (tạo lại `QLSV`) rồi `docker compose up -d --build`.

### Lệnh Docker hữu ích

```bash
docker compose ps
docker compose logs -f app
docker compose restart app
docker compose down              # tắt container, giữ volume
docker compose down -v           # tắt và xóa volume (mất dữ liệu)
```

### Gỡ lỗi nhanh (Docker)

- **502 / không vào được web:** xem `docker compose logs app` và `docker compose ps` — đợi `app` **healthy**.
- **App restart liên tục:** kiểm tra đã có database **`QLSV`** và mật khẩu `.env` khớp với SQL trong volume (đổi SA sau khi volume đã tạo rất khó; cách đơn giản: `down -v` và tạo lại — **mất dữ liệu**).
- **Cổng 80 hoặc 14333 bận:** đổi `NGINX_HTTP_PORT` / `SQLSERVER_HOST_PORT` trong `.env`.

---

## Stack Docker & Nginx (kỹ thuật tóm tắt)

| Hạng mục | Chi tiết |
|----------|----------|
| Thứ tự khởi động | `app` sau SQL healthy; `nginx` sau app healthy — giảm 502 lúc khởi động. |
| Actuator | Chỉ health/readiness phù hợp profile `docker`; không lộ chi tiết; **`/actuator`** chặn qua Nginx (404), healthcheck gọi trực tiếp vào container `app`. |
| Nginx | Gzip, buffer proxy, `server_tokens off`, timeout upload/đọc dài hơn, `client_max_body_size 5m`. |
| Dockerfile | Multi-stage build; user không phải root; timezone `Asia/Ho_Chi_Minh`; JAR `schoolmanager.jar`. |

Các file: `docker-compose.yml`, `Dockerfile`, `.env.example`, `docker/nginx/default.conf`, `docker/sql/init-qlsv.sql`, `src/main/resources/application-docker.properties`.

---

## Chạy local (không Docker)

1. Cài **JDK 21** và **SQL Server** (hoặc instance remote).
2. Sửa **`src/main/resources/application.properties`** (URL JDBC, user, mật khẩu, tên database — mặc định ví dụ `QLSV`).
3. Trong thư mục dự án:

```bash
bash mvnw spring-boot:run
```

Đúng goal Maven là **`spring-boot:run`** (có dấu gạch ngang), không phải `springboot:run`.

URL thường dùng: **http://localhost:8080/admin/students**, Swagger **http://localhost:8080/swagger-ui/index.html** (cổng theo `server.port`).

---

## Build JAR (tuỳ chọn)

```bash
bash mvnw -DskipTests package
java -jar target/schoolmanager.jar
```

Tên file JAR cố định nhờ `<finalName>schoolmanager</finalName>` trong `pom.xml`, khớp với bước copy trong `Dockerfile`.
